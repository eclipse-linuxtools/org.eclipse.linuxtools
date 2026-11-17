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
package org.eclipse.linuxtools.remote.proxy.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.profiling.launch.IRemoteEnvProxyManager;
import org.eclipse.linuxtools.profiling.launch.RemoteEnvProxyManager;
import org.junit.jupiter.api.Test;

public class RemoteProxyEnvManagerTest extends AbstractProxyTest {

	@Test
	public void testGetLocalEnv() throws CoreException {
		IRemoteEnvProxyManager proxy = new RemoteEnvProxyManager();
		assertEquals(System.getenv(), proxy.getEnv(localProject.getProject()));
		assertEquals(System.getenv(), proxy.getEnv(localProject.getLocationURI()));
	}

}
