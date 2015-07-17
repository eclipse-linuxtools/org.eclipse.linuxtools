/*******************************************************************************
 * Copyright (c) 2015 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wainer dos Santos Moschetta (IBM Corporation) - initial implementation
 *******************************************************************************/
package org.eclipse.linuxtools.remote.proxy.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.profiling.launch.IRemoteEnvProxyManager;
import org.eclipse.linuxtools.profiling.launch.RemoteEnvProxyManager;
import org.junit.Test;

/**
 * @author wainersm
 *
 */
public class RemoteProxyEnvManagerTest extends AbstractProxyTest {
	@Test
	public void testGetEnv() {
		Map<String, String> actualEnv = new HashMap<>();
		Map<String, String> expectedEnv = new HashMap<>();
		IRemoteEnvProxyManager proxy = new RemoteEnvProxyManager();

		/*
		 * Get local environment to compare with returned by the proxy
		 */
		expectedEnv= System.getenv();
		try {
			actualEnv = proxy.getEnv(localProject.getProject());
		} catch (CoreException e) {
			fail("Failed to get environment variables: " + e.getMessage());
		}
		assertEquals(expectedEnv.size(), actualEnv.size());
		assertEquals(expectedEnv.keySet(), actualEnv.keySet());
		assertEquals(expectedEnv.values(), actualEnv.values());

		/*
		 * Get remote environment to compare with returned by the proxy
		 */
		try {
			actualEnv = proxy.getEnv(syncProject.getProject());
		} catch (CoreException e) {
			fail("Failed to get remote environment variables: " + e.getMessage());
		}
		assertTrue(!actualEnv.isEmpty());
		// Bug 469184 - it should be able to filter out some variables
		for(Entry<String, String> entry: actualEnv.entrySet()) {
			assertTrue("It should not hold exported functions: " + entry.getKey(), !entry.getKey().matches("BASH_FUNC_.*"));
			assertTrue("It should not hold exported functions: " + entry.getKey(), !entry.getValue().matches("^\\("));
		}
	}

}
