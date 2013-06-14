/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.callgraph.launch.tests;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
	public void testCommand() throws FileNotFoundException {
		//Set up variables
		String testText = "CORRECT";
		String tempLocation = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString()
								+ "/DeleteThisScript.stp";

		//Create temporary file containing the test script
		File temporaryScript = new File(tempLocation);

		FileOutputStream output = new FileOutputStream(temporaryScript);
		PrintStream printer = new PrintStream(output);
		printer.println("probe begin { printf(\"" + testText + "\") exit() }");

		//Cleanup
		printer.close();
		temporaryScript.delete();
	}
}
