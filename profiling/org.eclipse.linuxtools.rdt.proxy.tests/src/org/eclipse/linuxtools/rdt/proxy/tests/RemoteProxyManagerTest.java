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
package org.eclipse.linuxtools.rdt.proxy.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.net.URI;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.internal.profiling.launch.LocalFileProxy;
import org.eclipse.linuxtools.internal.profiling.launch.LocalLauncher;
import org.eclipse.linuxtools.internal.rdt.proxy.RDTCommandLauncher;
import org.eclipse.linuxtools.internal.rdt.proxy.RDTFileProxy;
import org.eclipse.linuxtools.internal.ssh.proxy.SSHCommandLauncher;
import org.eclipse.linuxtools.internal.ssh.proxy.SSHFileProxy;
import org.eclipse.linuxtools.profiling.launch.IRemoteCommandLauncher;
import org.eclipse.linuxtools.profiling.launch.IRemoteFileProxy;
import org.eclipse.linuxtools.remote.proxy.tests.AbstractProxyTest;
import org.eclipse.ptp.rdt.sync.core.SyncConfig;
import org.junit.jupiter.api.Test;

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
			assertInstanceOf(LocalFileProxy.class, fp, "Should return a local file proxy");
			fp = proxyManager.getFileProxy(URI.create("file:/path/to/file"));
			assertInstanceOf(LocalFileProxy.class, fp, "Should return a local file proxy");
			fp = proxyManager.getFileProxy(localProject.getLocationURI());
			assertInstanceOf(LocalFileProxy.class, fp, "Should return a local file proxy");
			fp = proxyManager.getFileProxy(localProject.getProject());
			assertInstanceOf(LocalFileProxy.class, fp, "Should return a local file proxy");
			/*
			 * Test the proxy for remote URIs and project
			 */
			fp = proxyManager.getFileProxy(URI.create("ssh://" + CONNECTION_NAME + "/path/to/file"));
			assertInstanceOf(RDTFileProxy.class, fp, "Should have returned a remote file proxy");
			fp = proxyManager.getFileProxy(syncProject.getProject());
			assertInstanceOf(RDTFileProxy.class, fp, "Should have returned a remote file proxy");
			/*
			 * Test the proxy for jsch connection scheme
			 */
			fp = proxyManager.getFileProxy(URI.create("jsch://" + USERNAME + "@" + HOST + ":22/path/to/file"));
			assertInstanceOf(SSHFileProxy.class, fp, "Should have returned a remote file proxy");
		} catch (CoreException e) {
			fail("Should have returned a file proxy: " + e.getCause());
		}

		/*
		 * Test the proxy for unsupported URIs
		 */
		// As of org.eclipse.remote 2.0, remotetools scheme is no longer
		// support
		assertThrows(CoreException.class, ()-> proxyManager.getFileProxy(URI.create("remotetools://MyConnection/path/to/file")), "remotetools scheme should not be recognized");
	}

	@Test
	public void testGetLauncher() {
		IRemoteCommandLauncher cl;
		try {
			/*
			 * Test launcher got for local URIs and project
			 */
			cl = proxyManager.getLauncher(localProject.getLocationURI());
			assertInstanceOf(LocalLauncher.class, cl, "Should have returned a local launcher");
			cl = proxyManager.getLauncher(localProject.getProject());
			assertInstanceOf(LocalLauncher.class, cl, "Should have returned a local launcher");
			/*
			 * Test launcher got for remote project and URI
			 */
			cl = proxyManager.getLauncher(URI.create("ssh://" + CONNECTION_NAME + "/path/to/file"));
			assertInstanceOf(RDTCommandLauncher.class, cl, "Should have returned a remote file proxy");
			cl = proxyManager.getLauncher(syncProject.getProject());
			assertInstanceOf(RDTCommandLauncher.class, cl, "Should have returned a remote launcher");
			/*
			 * Test launcher got for jsch scheme
			 */
			cl = proxyManager.getLauncher(URI.create("jsch://" + USERNAME + "@" + HOST + ":22/path/to/file"));
			assertInstanceOf(SSHCommandLauncher.class, cl, "Should have returned a remote file proxy");
		} catch (CoreException e) {
			fail("Should have returned a launcher: " + e.getCause());
		}

		/*
		 * Test the proxy for unsupported URIs
		 */
		// As of org.eclipse.remote 2.0, remotetools scheme is no longer
		// support
		assertThrows(CoreException.class, ()->proxyManager.getLauncher(URI.create("remotetools://MyConnection/path/to/file")),"remotetools scheme should not be recognized");
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
			assertFalse(actualOS.isEmpty(), "Should have returned the OS name");
			actualOS = proxyManager.getOS(URI.create("file:/path/to/file"));
			assertNotNull(actualOS);
			assertFalse(actualOS.isEmpty(), "Should have returned the OS name");
			actualOS = proxyManager.getOS(localProject.getLocationURI());
			assertNotNull(actualOS);
			assertFalse(actualOS.isEmpty(), "Should have returned the OS name");
			actualOS = proxyManager.getOS(localProject.getProject());
			assertNotNull(actualOS);
			assertFalse(actualOS.isEmpty(), "Should have returned the OS name");
			/*
			 * Test got OS for remote URIs and project
			 */
			actualOS = proxyManager.getOS(syncProject.getProject());
			assertNotNull(actualOS);
			assertFalse(actualOS.isEmpty(), "Should have returned the OS name");
		} catch (CoreException e) {
			fail("Unabled to get OS name: " + e.getMessage());
		}

		/*
		 * Test the proxy for unsupported URIs
		 */
		// As of org.eclipse.remote 2.0, remotetools scheme is no longer
		// support
		assertThrows(CoreException.class, ()-> proxyManager.getOS(URI.create("remotetools://MyConnection/path/to/file")),"remotetools scheme should not be recognized");
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
