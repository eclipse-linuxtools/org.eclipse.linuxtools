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

import static org.junit.Assert.assertEquals;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.linuxtools.internal.callgraph.core.LaunchConfigurationConstants;
import org.eclipse.linuxtools.internal.callgraph.launch.LaunchStapGraph;
import org.eclipse.linuxtools.internal.callgraph.launch.SystemTapLaunchConfigurationDelegate;
import org.junit.Test;

public class ConfigurationTest extends AbstractStapTest{


	/**
	 * This test checks if the commands sent by SystemTap match exactly the options
	 * that are set. Uses the delegate.launch() function.
	 *
	 * Activates all options!
	 * @throws CoreException
	 */
	@Test
	public void testConfig() throws CoreException {

		LaunchStapGraph shortcut = new LaunchStapGraph();

		String testCDirectives = "-DRandomjunk -DMoreJunk";
		String testOutputPath = "/tmp/ThisFileDoesNothingDeleteIt";
		String testBinaryPath = "/path/to/binary";
		String testScriptPath = "/tmp/NotAScriptFile.stp";
		String testArguments = "/path/to/binary";
		int testPid = 413;
		int testBuffer = 100;
		int testPass = 10;

		ILaunchConfiguration config = shortcut.outsideGetLaunchConfigType()
				.newInstance(null, "Temp name");
		ILaunchConfigurationWorkingCopy wc = config
				.copy("Testing configuration");

		wc.setAttribute(LaunchConfigurationConstants.COMMAND_VERBOSE, 1);
		wc.setAttribute(LaunchConfigurationConstants.COMMAND_KEEP_TEMPORARY,
				true);
		wc.setAttribute(LaunchConfigurationConstants.COMMAND_GURU, true);
		wc.setAttribute(LaunchConfigurationConstants.COMMAND_PROLOGUE_SEARCH,
				true);
		wc.setAttribute(LaunchConfigurationConstants.COMMAND_NO_CODE_ELISION,
				true);
		wc.setAttribute(LaunchConfigurationConstants.COMMAND_DISABLE_WARNINGS,
				true);
		wc.setAttribute(LaunchConfigurationConstants.COMMAND_BULK_MODE, true);
		wc.setAttribute(LaunchConfigurationConstants.COMMAND_TIMING_INFO, true);
		wc.setAttribute(LaunchConfigurationConstants.COMMAND_SKIP_BADVARS, true);
		wc.setAttribute(LaunchConfigurationConstants.COMMAND_IGNORE_DWARF, true);
		wc.setAttribute(LaunchConfigurationConstants.COMMAND_TAPSET_COVERAGE,
				true);
		wc.setAttribute(LaunchConfigurationConstants.COMMAND_LEAVE_RUNNING,
				true);
		wc.setAttribute(LaunchConfigurationConstants.COMMAND_PASS, testPass);
		wc.setAttribute(LaunchConfigurationConstants.COMMAND_BUFFER_BYTES,
				testBuffer);
		wc.setAttribute(LaunchConfigurationConstants.COMMAND_TARGET_PID,
				testPid);

		wc.setAttribute(LaunchConfigurationConstants.COMMAND_C_DIRECTIVES,
				testCDirectives);
		wc.setAttribute(LaunchConfigurationConstants.BINARY_PATH,
				testBinaryPath);
		wc.setAttribute(LaunchConfigurationConstants.SCRIPT_PATH,
				testScriptPath);
		wc.setAttribute(LaunchConfigurationConstants.ARGUMENTS, testArguments);
		wc.setAttribute(LaunchConfigurationConstants.OUTPUT_PATH,
				testOutputPath);

		config = wc.doSave();

		SystemTapLaunchConfigurationDelegate del = new SystemTapLaunchConfigurationDelegate();
		del.launch(config, "profile", null, null);

		assertEquals("stap -v -p" + testPass + " -k -g -P -u -w -b -t -s"
				+ testBuffer + " -x" + testPid + " " + testCDirectives
				+ " -F --skip-badvars --ignore-dwarf -q " + " -c '" + testBinaryPath
				+ "' " + testScriptPath + " --runtime=dyninst " + testArguments + " >& "
				+ testOutputPath,
				del.generateCommand(config));

		killStap();
	}




}
