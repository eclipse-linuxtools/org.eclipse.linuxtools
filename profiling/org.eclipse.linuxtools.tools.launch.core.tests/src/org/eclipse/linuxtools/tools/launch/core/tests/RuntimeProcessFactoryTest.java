/*******************************************************************************
 * Copyright (c) 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.tools.launch.core.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.remote.proxy.tests.AbstractProxyTest;
import org.eclipse.linuxtools.tools.launch.core.factory.RuntimeProcessFactory;
import org.junit.Test;

public class RuntimeProcessFactoryTest extends AbstractProxyTest {

	@Test
	public void whichCommandTest() {
		String actualCmd = null, expectedCmdPath, testCaseMsg = "";
		IProject actualProject;
		boolean expectedExist;

		Object[][] testCases = {
				{localProject.getProject(), "ls", true},
				{localProject.getProject(), "notexistcmd", false},
				{syncProject.getProject(), "ls", true},
				{syncProject.getProject(), "notexistcmd", false}
		};

		for(Object[] params : testCases) {
			try {
				actualProject = (IProject) params[0];
				assertNotNull(actualProject);
				actualCmd = (String) params[1];
				expectedExist = (boolean) params[2];
				testCaseMsg = "(" + actualProject.getName() + "): ";

				expectedCmdPath = RuntimeProcessFactory.getFactory().whichCommand(actualCmd, actualProject);
				assertFalse(expectedCmdPath.isEmpty());
				assertTrue(Path.isValidPosixPath(expectedCmdPath));
				// If command does not exist it should return the command's name.
				if(!expectedExist) {
					assertTrue(testCaseMsg + "Command not exist",
							actualCmd.contentEquals(expectedCmdPath));
				} else {
					assertFalse(testCaseMsg + "Should had return a path different from command name",
							actualCmd.contentEquals(expectedCmdPath));
				}
			} catch (IOException e) {
				fail(testCaseMsg + "Should had returned valid path to '" + actualCmd + "' command");
			}
		}
	}
}
