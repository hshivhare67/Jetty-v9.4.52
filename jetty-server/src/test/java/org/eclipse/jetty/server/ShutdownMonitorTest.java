//
//  ========================================================================
//  Copyright (c) 1995-2022 Mort Bay Consulting Pty Ltd and others.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//

package org.eclipse.jetty.server;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Properties;

import org.eclipse.jetty.util.thread.ShutdownThread;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ShutdownMonitorTest
{
    @BeforeEach
    public void reset()
    {
        clearShutdownMonitorSystemProperties();
    }

    @AfterEach
    public void shutdown()
    {
        clearShutdownMonitorSystemProperties();
        ShutdownMonitor.reset();
    }

    private void clearShutdownMonitorSystemProperties()
    {
        // clear out system properties set in individual test cases
        Properties systemProperties = System.getProperties();
        systemProperties.remove("STOP.EXIT");
        systemProperties.remove("DEBUG");
        systemProperties.remove("STOP.HOST");
        systemProperties.remove("STOP.PORT");
        systemProperties.remove("STOP.KEY");
    }

    @Test
    public void testStatus() throws Exception
    {
        ShutdownMonitor monitor = ShutdownMonitor.getInstance();
        // monitor.setDebug(true);
        monitor.setPort(0);
        monitor.setExitVm(false);
        monitor.start();
        String key = monitor.getKey();
        int port = monitor.getPort();

        // Try more than once to be sure that the ServerSocket has not been closed.
        for (int i = 0; i < 2; ++i)
        {
            try (Socket socket = new Socket("localhost", port))
            {
                OutputStream output = socket.getOutputStream();
                String command = "status";
                output.write((key + "\r\n" + command + "\r\n").getBytes());
                output.flush();

                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String reply = input.readLine();
                assertEquals("OK", reply);
                // Socket must be closed afterwards.
                assertNull(input.readLine());
            }
        }
    }

    @Disabled("Issue #2626")
    @Test
    public void testStartStopDifferentPortDifferentKey() throws Exception
    {
        testStartStop(false);
    }

    @Disabled("Issue #2626")
    @Test
    public void testStartStopSamePortDifferentKey() throws Exception
    {
        testStartStop(true);
    }

    private void testStartStop(boolean reusePort) throws Exception
    {
        ShutdownMonitor monitor = ShutdownMonitor.getInstance();
        // monitor.setDebug(true);
        monitor.setPort(0);
        monitor.setExitVm(false);
        monitor.start();
        String key = monitor.getKey();
        int port = monitor.getPort();

        // try starting a 2nd time (should be ignored)
        monitor.start();

        stop("stop", port, key, true);
        monitor.await();
        assertTrue(!monitor.isAlive());

        // Should be able to change port and key because it is stopped.
        monitor.setPort(reusePort ? port : 0);
        String newKey = "foo";
        monitor.setKey(newKey);
        monitor.start();

        key = monitor.getKey();
        assertEquals(newKey, key);
        port = monitor.getPort();
        assertTrue(monitor.isAlive());

        stop("stop", port, key, true);
        monitor.await();
        assertTrue(!monitor.isAlive());
    }

    @Test
    public void testNoExitSystemProperty() throws Exception
    {
        System.setProperty("STOP.EXIT", "false");
        ShutdownMonitor.reset(); // this creates a new ShutdownMonitor singleton, so we do this now to get the System Property set properly
        ShutdownMonitor monitor = ShutdownMonitor.getInstance();
        monitor.setPort(0);
        assertFalse(monitor.isExitVm());
        monitor.start();

        try (CloseableServer server = new CloseableServer())
        {
            server.setStopAtShutdown(true);
            server.start();

            //shouldn't be registered for shutdown on jvm
            assertTrue(ShutdownThread.isRegistered(server));
            assertTrue(ShutdownMonitor.isRegistered(server));

            String key = monitor.getKey();
            int port = monitor.getPort();

            stop("stop", port, key, true);
            monitor.await();

            assertTrue(!monitor.isAlive());
            assertTrue(server.stopped);
            assertTrue(!server.destroyed);
            assertTrue(!ShutdownThread.isRegistered(server));
            assertTrue(!ShutdownMonitor.isRegistered(server));
        }
    }

    /*
     * Disable these config tests because ShutdownMonitor is a 
     * static singleton that cannot be unset, and thus would
     * need each of these methods executed it its own jvm -
     * current surefire settings only fork for a single test 
     * class.
     * 
     * Undisable to test individually as needed.
     */
    @Disabled
    @Test
    public void testExitVmDefault() throws Exception
    {
        //Test that the default is to exit
        ShutdownMonitor monitor = ShutdownMonitor.getInstance();
        monitor.setPort(0);
        assertTrue(monitor.isExitVm());
    }

    @Disabled
    @Test
    public void testExitVmTrue() throws Exception
    {
        //Test setting exit true
        System.setProperty("STOP.EXIT", "true");
        ShutdownMonitor.reset(); // this creates a new ShutdownMonitor singleton, so we do this now to get the System Property set properly
        ShutdownMonitor monitor = ShutdownMonitor.getInstance();
        monitor.setPort(0);
        assertTrue(monitor.isExitVm());
    }
    
    @Disabled
    @Test
    public void testExitVmFalse() throws Exception
    {
        //Test setting exit false
        System.setProperty("STOP.EXIT", "false");
        ShutdownMonitor.reset(); // this creates a new ShutdownMonitor singleton, so we do this now to get the System Property set properly
        ShutdownMonitor monitor = ShutdownMonitor.getInstance();
        monitor.setPort(0);
        assertFalse(monitor.isExitVm());
    }
    
    @Disabled
    @Test
    public void testForceStopCommand() throws Exception
    {
        ShutdownMonitor monitor = ShutdownMonitor.getInstance();
        // monitor.setDebug(true);
        monitor.setPort(0);
        monitor.setExitVm(false);
        monitor.start();

        try (CloseableServer server = new CloseableServer())
        {
            server.start();

            //shouldn't be registered for shutdown on jvm
            assertTrue(!ShutdownThread.isRegistered(server));
            assertTrue(ShutdownMonitor.isRegistered(server));

            String key = monitor.getKey();
            int port = monitor.getPort();

            stop("forcestop", port, key, true);
            monitor.await();

            assertTrue(!monitor.isAlive());
            assertTrue(server.stopped);
            assertTrue(!server.destroyed);
            assertTrue(!ShutdownThread.isRegistered(server));
            assertTrue(!ShutdownMonitor.isRegistered(server));
        }
    }

    @Test
    public void testOldStopCommandWithStopOnShutdownTrue() throws Exception
    {
        ShutdownMonitor monitor = ShutdownMonitor.getInstance();
        // monitor.setDebug(true);
        monitor.setPort(0);
        monitor.setExitVm(false);
        monitor.start();

        try (CloseableServer server = new CloseableServer())
        {
            server.setStopAtShutdown(true);
            server.start();

            //should be registered for shutdown on exit
            assertTrue(ShutdownThread.isRegistered(server));
            assertTrue(ShutdownMonitor.isRegistered(server));

            String key = monitor.getKey();
            int port = monitor.getPort();

            stop("stop", port, key, true);
            monitor.await();

            assertTrue(!monitor.isAlive());
            assertTrue(server.stopped);
            assertTrue(!server.destroyed);
            assertTrue(!ShutdownThread.isRegistered(server));
            assertTrue(!ShutdownMonitor.isRegistered(server));
        }
    }

    @Test
    public void testOldStopCommandWithStopOnShutdownFalse() throws Exception
    {
        ShutdownMonitor monitor = ShutdownMonitor.getInstance();
        // monitor.setDebug(true);
        monitor.setPort(0);
        monitor.setExitVm(false);
        monitor.start();

        try (CloseableServer server = new CloseableServer())
        {
            server.setStopAtShutdown(false);
            server.start();

            assertTrue(!ShutdownThread.isRegistered(server));
            assertTrue(ShutdownMonitor.isRegistered(server));

            String key = monitor.getKey();
            int port = monitor.getPort();

            stop("stop", port, key, true);
            monitor.await();

            assertTrue(!monitor.isAlive());
            assertTrue(!server.stopped);
            assertTrue(!server.destroyed);
            assertTrue(!ShutdownThread.isRegistered(server));
            assertTrue(ShutdownMonitor.isRegistered(server));
        }
    }

    public void stop(String command, int port, String key, boolean check) throws Exception
    {
        // System.out.printf("Attempting to send " + command + " to localhost:%d (%b)%n", port, check);
        try (Socket s = new Socket(InetAddress.getByName("127.0.0.1"), port))
        {
            // send stop command
            try (OutputStream out = s.getOutputStream())
            {
                out.write((key + "\r\n" + command + "\r\n").getBytes());
                out.flush();

                if (check)
                {
                    // check for stop confirmation
                    LineNumberReader lin = new LineNumberReader(new InputStreamReader(s.getInputStream()));
                    String response;
                    if ((response = lin.readLine()) != null)
                        assertEquals("Stopped", response);
                    else
                        throw new IllegalStateException("No stop confirmation");
                }
            }
        }
    }

    public class CloseableServer extends Server implements Closeable
    {
        boolean destroyed = false;
        boolean stopped = false;

        @Override
        protected void doStop() throws Exception
        {
            stopped = true;
            super.doStop();
        }

        @Override
        public void destroy()
        {
            destroyed = true;
            super.destroy();
        }

        @Override
        protected void doStart() throws Exception
        {
            stopped = false;
            destroyed = false;
            super.doStart();
        }

        @Override
        public void close()
        {
            try
            {
                stop();
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
    }
}
