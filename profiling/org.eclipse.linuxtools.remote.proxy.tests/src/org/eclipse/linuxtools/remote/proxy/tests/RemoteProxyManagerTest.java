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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URI;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.internal.profiling.launch.LocalFileProxy;
import org.eclipse.linuxtools.internal.profiling.launch.LocalLauncher;
import org.eclipse.linuxtools.internal.rdt.proxy.RDTCommandLauncher;
import org.eclipse.linuxtools.internal.rdt.proxy.RDTFileProxy;
import org.eclipse.linuxtools.profiling.launch.IRemoteCommandLauncher;
import org.eclipse.linuxtools.profiling.launch.IRemoteFileProxy;
import org.eclipse.ptp.rdt.sync.core.SyncConfig;

@SuppressWarnings("restriction")
public class RemoteProxyManagerTest extends AbstractProxyTest {

	@Test
	public void testGetFileProxy() {
		IRemoteFileProxy fp;
		try {
			/*
			 * Test the proxy for local URIs and project
			 */
			fp = proxyManager.getFileProxy(URI.create("/path/to/file"));
			assertTrue("Should return a local file proxy", fp instanceof LocalFileProxy);
			fp = proxyManager.getFileProxy(URI.create("file:/path/to/file"));
			assertTrue("Should return a local file proxy", fp instanceof LocalFileProxy);
			fp = proxyManager.getFileProxy(localProject.getLocationURI());
			assertTrue("Should return a local file proxy", fp instanceof LocalFileProxy);
			fp = proxyManager.getFileProxy(localProject.getProject());
			assertTrue("Should return a local file proxy", fp instanceof LocalFileProxy);
			/*
			 * Test the proxy for remote URIs and project
			 */
			fp = proxyManager.getFileProxy(URI.create("ssh://" + CONNECTION_NAME + "/path/to/file"));
			assertTrue("Should have returned a remote file proxy", fp instanceof RDTFileProxy);
			fp = proxyManager.getFileProxy(syncProject.getProject());
			assertTrue("Should have returned a remote file proxy", fp instanceof RDTFileProxy);
		} catch (CoreException e) {
			fail("Should have returned a file proxy: " + e.getCause());
		}

		/*
		 * Test the proxy for unsupported URIs
		 */
		try {
			// As of org.eclipse.remote 2.0, remotetools scheme is no longer
			// support
			fp = proxyManager.getFileProxy(URI.create("remotetools://MyConnection/path/to/file"));
			fail("remotetools scheme should not be recognized");
		} catch (CoreException e) {
			assertTrue(e.getMessage(), true);
		}
	}

	@Test
	public void testGetLauncher() {
		IRemoteCommandLauncher cl;
		try {
			/*
			 * Test launcher got for local URIs and project
			 */
			cl = proxyManager.getLauncher(localProject.getLocationURI());
			assertTrue("Should have returned a local launcher", cl instanceof LocalLauncher);
			cl = proxyManager.getLauncher(localProject.getProject());
			assertTrue("Should have returned a local launcher", cl instanceof LocalLauncher);
			/*
			 * Test launcher got for remote project and URI
			 */
			cl = proxyManager.getLauncher(URI.create("ssh://" + CONNECTION_NAME + "/path/to/file"));
			assertTrue("Should have returned a remote file proxy", cl instanceof RDTCommandLauncher);
			cl = proxyManager.getLauncher(syncProject.getProject());
			assertTrue("Should have returned a remote launcher", cl instanceof RDTCommandLauncher);
		} catch (CoreException e) {
			fail("Should have returned a launcher: " + e.getCause());
		}

		/*
		 * Test the proxy for unsupported URIs
		 */
		try {
			// As of org.eclipse.remote 2.0, remotetools scheme is no longer
			// support
			cl = proxyManager.getLauncher(URI.create("remotetools://MyConnection/path/to/file"));
			fail("remotetools scheme should not be recognized");
		} catch (CoreException e) {
			assertTrue(e.getMessage(),true);
		}
	}

	@Test
	public void testGetOS() {
		String actualOS = null;
		try {
			/*
			 * Test got OS for local URIs and project
			 */
			actualOS = proxyManager.getOS(URI.create("/path/to/file"));
			assertNotNull(actualOS);
			assertTrue("Should have returned the OS name", !actualOS.isEmpty());
			actualOS = proxyManager.getOS(URI.create("file:/path/to/file"));
			assertNotNull(actualOS);
			assertTrue("Should have returned the OS name", !actualOS.isEmpty());
			actualOS = proxyManager.getOS(localProject.getLocationURI());
			assertNotNull(actualOS);
			assertTrue("Should have returned the OS name", !actualOS.isEmpty());
			actualOS = proxyManager.getOS(localProject.getProject());
			assertNotNull(actualOS);
			assertTrue("Should have returned the OS name", !actualOS.isEmpty());
			/*
			 * Test got OS for remote URIs and project
			 */
			actualOS = proxyManager.getOS(syncProject.getProject());
			assertNotNull(actualOS);
			assertTrue("Should have returned the OS name", !actualOS.isEmpty());
		} catch (CoreException e) {
			fail("Unabled to get OS name: " + e.getMessage());
		}

		/*
		 * Test the proxy for unsupported URIs
		 */
		try {
			// As of org.eclipse.remote 2.0, remotetools scheme is no longer
			// support
			actualOS = proxyManager.getOS(URI.create("remotetools://MyConnection/path/to/file"));
			fail("remotetools scheme should not be recognized");
		} catch (CoreException e) {
			assertTrue(e.getMessage(),true);
		}
	}

	@Test
	public void testGetRemoteProjectLocationOnSyncProj() {
		try {
			String actualLocation = proxyManager.getRemoteProjectLocation(syncProject.getProject());
			SyncConfig config = getSyncConfig(syncProject.getProject());
			assertNotNull(config);
			assertEquals(connection.getConnectionType().getScheme(), URI.create(actualLocation).getScheme());
			assertEquals(config.getConnectionName(), URI.create(actualLocation).getAuthority());
			assertEquals(config.getLocation(),URI.create(actualLocation).getPath());
		} catch (CoreException e) {
			fail("Should have returned the remote project location: " + e.getMessage());
		}
	}
}
