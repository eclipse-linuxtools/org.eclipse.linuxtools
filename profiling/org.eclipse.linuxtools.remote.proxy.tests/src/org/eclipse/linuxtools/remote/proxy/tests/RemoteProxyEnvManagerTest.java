/*******************************************************************************
 * Copyright (c) 2015, 2018 IBM Corporation and others.
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
package org.eclipse.linuxtools.remote.proxy.tests;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.profiling.launch.IRemoteEnvProxyManager;
import org.eclipse.linuxtools.profiling.launch.RemoteEnvProxyManager;
import org.eclipse.remote.core.IRemoteConnection;
import org.junit.jupiter.api.Test;

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
		assertFalse(actualEnv.isEmpty());
		// Bug 469184 - it should be able to filter out some variables
		for(Entry<String, String> entry: actualEnv.entrySet()) {
			assertFalse(entry.getKey().matches("BASH_FUNC_.*"), "It should not hold exported functions: " + entry.getKey());
			assertFalse(entry.getValue().matches("^\\("), "It should not hold exported functions: " + entry.getKey());
		}

		/*
		 * Test it opens connection to get the env
		 */
		IRemoteConnection conn = getConnection();
		assertNotNull(conn);
		conn.close();
		assertFalse(conn.isOpen());
		proxy = new RemoteEnvProxyManager();
		try {
			actualEnv = proxy.getEnv(syncProject.getProject());
			assertTrue(actualEnv.size() > 0);
		} catch (CoreException e) {
			fail("Failed to get env when connection is closed: " + e.getMessage());
		}
	}

}
