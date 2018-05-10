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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.eclipse.core.resources.ResourcesPlugin;
import org.junit.Test;

public class SystemTapCommandTest {

    /**
     * Tests if SystemTapCommand is properly obtaining output from the runtime
     * process
     *
     * @throws FileNotFoundException
     */
    @Test
    public void testCommand() throws IOException {
        // Set up variables
        String testText = "CORRECT";
        String tempLocation = ResourcesPlugin.getWorkspace().getRoot()
                .getLocation().toString()
                + "/DeleteThisScript.stp";

        // Create temporary file containing the test script
        File temporaryScript = new File(tempLocation);

        try (FileOutputStream output = new FileOutputStream(temporaryScript);
                PrintStream printer = new PrintStream(output)) {
            printer.println("probe begin { printf(\"" + testText
                    + "\") exit() }");
        }
        // Cleanup
        temporaryScript.delete();
    }
}
