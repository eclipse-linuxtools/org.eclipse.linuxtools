/*******************************************************************************
 * Copyright (c) 2015, 2026 IBM Corporation and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wainer dos Santos Moschetta (IBM Corporation) - initial implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rdt.proxy.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map.Entry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.internal.profiling.launch.LocalLauncher;
import org.eclipse.linuxtools.profiling.launch.IRemoteCommandLauncher;
import org.eclipse.linuxtools.remote.proxy.tests.AbstractProxyTest;
import org.junit.jupiter.api.Test;

@SuppressWarnings("restriction")
public class CommandLauncherProxyTest extends AbstractProxyTest {

	@Test
	public void testLocalCommandLauncher() {
		IRemoteCommandLauncher cl = null;
		Process p = null;
		InputStream actualIS = null, expectedIS = null;
		IPath commandPath, changeToDirectory;
		String[] args, env;
		try {
			cl =  proxyManager.getLauncher(localProject.getProject());
			assertInstanceOf(LocalLauncher.class, cl, "Should have returned a local launcher");
		} catch (CoreException e) {
			fail("Should have returned a launcher: " + e.getCause());
		}
		/*
		 * Prepare arguments for the test
		 */
		commandPath = new Path("/bin/echo");
		args = new String[]{"proxy-test"};
		StringBuilder fullCmd = new StringBuilder();
		fullCmd.append(commandPath.toOSString());
		for(String s: args) {
			fullCmd.append(" " + s);
		}
		// Use local env variables
		ArrayList<String> envList = new ArrayList<>();
		for(Entry<String, String> entry : System.getenv().entrySet()) {
			envList.add(entry.getKey()+"="+entry.getValue());
		}
		env = envList.toArray(new String[]{});
		changeToDirectory = new Path("/tmp");

		/*
		 * Run and get results using the proxy
		 */
		try {
			p = cl.execute(commandPath, args, env, changeToDirectory, new NullProgressMonitor());
			assertNotNull(p, cl.getErrorMessage());
			while(p.isAlive()){}
			// Call to waitFor() will drive to empty result
			//p.waitFor();
			assertEquals(0, p.exitValue(), "Process exited with failure");
		} catch (Exception e) {
			fail("Unable to execute " + fullCmd.toString() + " on local machine: " + e.getMessage());
		}

		actualIS = p.getInputStream();
		assertNotNull(actualIS);

		/*
		 * Run and get results using java Runtime
		 */
		try {
			Process expectedProcess = Runtime.getRuntime().exec(fullCmd.toString());
			expectedProcess.waitFor();
			expectedIS = expectedProcess.getInputStream();
			assertNotNull(expectedIS);
		} catch (Exception e) {
			fail("Unable to execute " + fullCmd.toString() + " on  local using Runtime.exec: " + e.getMessage());
		}

		/*
		 * Finally compare results obtained
		 */
		int va=0, ve=0;
		
		do {
			try {
				va = actualIS.read();
				ve = expectedIS.read();
			} catch (IOException e) {
				fail("Unable to read from Input Stream: " + e.getMessage());
			}
			assertEquals(ve, va, "Local proxy command output differs from Runtime.exec");
		} while(va != -1);
	}
}
