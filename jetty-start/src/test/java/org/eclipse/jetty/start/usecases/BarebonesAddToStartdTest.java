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

package org.eclipse.jetty.start.usecases;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jetty.toolchain.test.PathAssert;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;

public class BarebonesAddToStartdTest extends AbstractUseCase
{
    @Test
    public void testBarebonesAddToStartdTest() throws Exception
    {
        setupStandardHomeDir();

        Files.write(baseDir.resolve("start.ini"),
            Collections.singletonList(
                "--module=main"
            ),
            StandardCharsets.UTF_8);

        // === Prepare Jetty Base using Main
        List<String> prepareArgs = Arrays.asList(
            "--testing-mode",
            "--create-startd",
            "--add-to-start=optional"
        );
        exec(prepareArgs, true);

        // === Execute Main
        List<String> runArgs = Collections.emptyList();
        ExecResults results = exec(runArgs, false);

        // === Validate Resulting XMLs
        List<String> expectedXmls = Arrays.asList(
            "${jetty.home}/etc/optional.xml",
            "${jetty.home}/etc/base.xml",
            "${jetty.home}/etc/main.xml"
        );
        List<String> actualXmls = results.getXmls();
        assertThat("XML Resolution Order", actualXmls, contains(expectedXmls.toArray()));

        // === Validate Resulting LIBs
        List<String> expectedLibs = Arrays.asList(
            "${jetty.home}/lib/base.jar",
            "${jetty.home}/lib/main.jar",
            "${jetty.home}/lib/other.jar",
            "${jetty.home}/lib/optional.jar"
        );
        List<String> actualLibs = results.getLibs();
        assertThat("Libs", actualLibs, containsInAnyOrder(expectedLibs.toArray()));

        // === Validate Resulting Properties
        Set<String> expectedProperties = new HashSet<>();
        expectedProperties.add("main.prop=value0");
        expectedProperties.add("optional.prop=value0");
        List<String> actualProperties = results.getProperties();
        assertThat("Properties", actualProperties, containsInAnyOrder(expectedProperties.toArray()));

        // === Validate Specific Jetty Base Files/Dirs Exist
        PathAssert.assertDirExists("Required Directory: maindir/", results.baseHome.getPath("maindir/"));
        PathAssert.assertFileExists("Required File: start.d/start.ini", results.baseHome.getPath("start.d/start.ini"));
        PathAssert.assertFileExists("Required File: start.d/optional.ini", results.baseHome.getPath("start.d/optional.ini"));
    }
}
