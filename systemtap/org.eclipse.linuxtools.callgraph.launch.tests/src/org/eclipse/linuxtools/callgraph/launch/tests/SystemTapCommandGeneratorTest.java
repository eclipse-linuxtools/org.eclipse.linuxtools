/*******************************************************************************
 * Copyright (c) 2009, 2018 Red Hat, Inc.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.callgraph.launch.tests;

import static org.junit.Assert.*;

import java.io.File;

import org.eclipse.linuxtools.internal.callgraph.core.PluginConstants;
import org.eclipse.linuxtools.internal.callgraph.core.SystemTapCommandGenerator;
import org.junit.Test;

public class SystemTapCommandGeneratorTest extends AbstractStapTest {

    // HACK TO GET THE PATH TO THE TESTING PROJECT
    File file = new File("");
    private String location = file.getAbsolutePath() + "/";
    SystemTapCommandGenerator stapgen = new SystemTapCommandGenerator();

    @Test
    public void testExecutionWithScriptAndBinaryAndArgument() {

        String binaryFilePath = location + "factorial";
        String scriptPath = location + "function_count.stp";

        // RUN
        String cmd = SystemTapCommandGenerator.generateCommand(scriptPath,
                binaryFilePath, "", true, true, binaryFilePath, "",
                PluginConstants.STAP_PATH);

        assertEquals("stap -c '" + binaryFilePath + "' " + scriptPath
                + " --runtime=dyninst " + binaryFilePath, cmd);
        killStap();
        // END
    }

    @Test
    public void testScriptExecution() {

        String scriptPath = location + "simple.stp";

        // RUN
        String cmd = SystemTapCommandGenerator.generateCommand(scriptPath, "",
                "", false, false, "", "", PluginConstants.STAP_PATH);

        assertEquals("stap " + scriptPath, cmd);
        // END
    }

    @Test
    public void testExecutionWithScriptAndBinary() {

        // RUN
        String binaryFilePath = location + "factorial";
        String scriptPath = location + "allsyscall.stp";

        String cmd = SystemTapCommandGenerator.generateCommand(scriptPath,
                binaryFilePath, "", true, false, "", "",
                PluginConstants.STAP_PATH);

        assertEquals("stap -c '" + binaryFilePath + "' " + scriptPath, cmd);
        // END

        killStap();
    }

}
