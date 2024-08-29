/*******************************************************************************
 * Copyright (c) 2016, 2018 IBM Corporation and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.tools.launch.core.tests;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.remote.proxy.tests.AbstractProxyTest;
import org.eclipse.linuxtools.tools.launch.core.factory.RuntimeProcessFactory;
import org.junit.jupiter.api.Test;

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
					assertTrue(actualCmd.contentEquals(expectedCmdPath), testCaseMsg + "Command not exist");
				} else {
					assertFalse(actualCmd.contentEquals(expectedCmdPath), testCaseMsg + "Should had return a path different from command name");
				}
			} catch (IOException e) {
				fail(testCaseMsg + "Should had returned valid path to '" + actualCmd + "' command");
			}
		}
	}
}
