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
import java.io.IOException;

import org.eclipse.linuxtools.callgraph.core.SystemTapCommandGenerator;

import junit.framework.TestCase;

public class SystemTapCommandGeneratorTest extends TestCase{ 
	
	//HACK TO GET THE PATH TO THE TESTING PROJECT
	File file = new File("");
	private String location = file.getAbsolutePath() + "/";
	SystemTapCommandGenerator stapgen = new SystemTapCommandGenerator();
	
	public void testExecutionWithScriptAndBinaryAndArgument(){
		
		System.out.println("\n\nLaunching SystemTapCommandGeneratorTest\n");
		String binaryFilePath = location + "factorial";
		String scriptPath = location + "function_count.stp";
		
		//RUN
		stapgen
				.generateCommand(
						scriptPath,
						binaryFilePath,
						"",
						true,
						true,
						binaryFilePath, "");
		
		assertEquals(
				"stap -c '"+binaryFilePath+"' "+scriptPath+ " " +binaryFilePath,
				stapgen.getExecuteCommand());
		killStap();
		//END
		}
	
	public void testScriptExecution(){

		String scriptPath = location + "simple.stp";
		
		//RUN
		stapgen
		.generateCommand(
				scriptPath,
				"",
				"",
				false,
				false,
				"", "");
		
		assertEquals(
				"stap "+scriptPath,
				stapgen.getExecuteCommand());
		//END
	}

	public void testExecutionWithScriptAndBinary() {

		// RUN
		String binaryFilePath = location + "factorial";
		String scriptPath = location + "allsyscall.stp";

		stapgen.generateCommand(scriptPath, binaryFilePath, "", true, false, "", "");

		assertEquals("stap -c '" + binaryFilePath + "' " + scriptPath, stapgen
				.getExecuteCommand());
		// END
		
		killStap();
	}

	
	public void killStap() {
		Runtime run = Runtime.getRuntime();
		try {
			run.exec("kill stap");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
