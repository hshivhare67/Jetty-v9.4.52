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

package org.eclipse.jetty.server.session;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.thread.Scheduler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * HouseKeeperTest
 */
public class HouseKeeperTest
{
    public class TestHouseKeeper extends HouseKeeper
    {
        public Scheduler getScheduler()
        {
            return _scheduler;
        }

        public Scheduler.Task getTask()
        {
            return _task;
        }
        
        public Runner getRunner()
        {
            return _runner;
        }
        
        public boolean isOwnScheduler()
        {
            return _ownScheduler;
        }
    }

    public class TestSessionIdManager extends DefaultSessionIdManager
    {
        public TestSessionIdManager(Server server)
        {
            super(server);
        }

        @Override
        public Set<SessionHandler> getSessionHandlers()
        {
            return Collections.singleton(new SessionHandler());
        }
    }
    
    @Test
    public void testHouseKeeper() throws Exception
    {
        HouseKeeper t = new TestHouseKeeper();
        assertThrows(IllegalStateException.class, () -> t.start());
        
        TestHouseKeeper hk = new TestHouseKeeper();
        hk.setSessionIdManager(new TestSessionIdManager(new Server()));
        hk.setIntervalSec(-1);
        hk.start(); //no scavenging
        
        //check that the housekeeper isn't running
        assertNull(hk.getRunner());
        assertNull(hk.getTask());
        assertNull(hk.getScheduler());
        assertFalse(hk.isOwnScheduler());
        hk.stop();
        assertNull(hk.getRunner());
        assertNull(hk.getTask());
        assertNull(hk.getScheduler());
        assertFalse(hk.isOwnScheduler());
        
        //set the interval but don't start it
        hk.setIntervalSec(10000);
        assertNull(hk.getRunner());
        assertNull(hk.getTask());
        assertNull(hk.getScheduler());
        assertFalse(hk.isOwnScheduler());
        
        //now start it
        hk.start();
        assertNotNull(hk.getRunner());
        assertNotNull(hk.getTask());
        assertNotNull(hk.getScheduler());
        assertTrue(hk.isOwnScheduler());
        
        //stop it
        hk.stop();
        assertNull(hk.getRunner());
        assertNull(hk.getTask());
        assertNull(hk.getScheduler());
        assertFalse(hk.isOwnScheduler());
        
        //start it, but set a different interval after start
        hk.start();
        Scheduler.Task oldTask = hk.getTask();
        hk.setIntervalSec(50000);
        assertTrue(hk.getIntervalSec() >= 50000);
        assertNotNull(hk.getRunner());
        assertNotNull(hk.getTask());
        //Note: it would be nice to test if the old task was
        //cancelled, but the Scheduler.Task interface does not
        //provide that functionality.
        assertNotSame(oldTask, hk.getTask());
        assertNotNull(hk.getScheduler());
        assertTrue(hk.isOwnScheduler());
    }
}
