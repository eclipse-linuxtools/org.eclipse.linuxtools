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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.internal.profiling.launch.LocalFileProxy;
import org.eclipse.linuxtools.internal.rdt.proxy.RDTFileProxy;
import org.eclipse.linuxtools.profiling.launch.IRemoteFileProxy;
import org.eclipse.ptp.rdt.sync.core.SyncConfig;
import org.eclipse.ptp.rdt.sync.core.exceptions.MissingConnectionException;
import org.eclipse.remote.core.IRemoteConnection;
import org.junit.Test;

@SuppressWarnings("restriction")
public class FileProxyTest extends AbstractProxyTest {

	@Test
	public void testRemoteFileProxyOnSyncProject() {
		IRemoteFileProxy fileProxy = null;
		try {
			fileProxy =  proxyManager.getFileProxy(syncProject.getProject());
			assertTrue("Should have returned a remote launcher", fileProxy instanceof RDTFileProxy);
		} catch (CoreException e) {
			fail("Should have returned a launcher: " + e.getCause());
		}
		String ds = fileProxy.getDirectorySeparator();
		assertNotNull(ds);

		SyncConfig config = getSyncConfig(syncProject.getProject());
		String projectLocation = config.getLocation();
		assertNotNull(projectLocation);
		IRemoteConnection conn = null;
		String connScheme = null;
		try {
			conn = config.getRemoteConnection();
			connScheme = conn.getConnectionType().getScheme();
		} catch (MissingConnectionException e) {
			fail("Unabled to get remote connection: " + e.getMessage());
		}

		/*
		 *  Test getResource()
		 */
		IFileStore fs = fileProxy.getResource(projectLocation);
		assertNotNull(fs);
		assertEquals("Remote connection and FileStore schemes diverge", connScheme, fs.toURI().getScheme());
		//assertTrue(fs.fetchInfo().isDirectory());

		/*
		 * Test getWorkingDir()
		 */
		URI workingDir = fileProxy.getWorkingDir();
		assertNotNull(workingDir);
		assertEquals("Remote connection and URI schemes diverge", connScheme, workingDir.getScheme());

		/*
		 * Test toPath()
		 */
		URI uri = null;
		try {
			uri = new URI(connScheme, conn.getName(), projectLocation, null, null);
		} catch (URISyntaxException e) {
			fail("Failed to build URI for the test: " + e.getMessage());
		}
		assertEquals(projectLocation, fileProxy.toPath(uri));
	}

	@Test
	public void testLocalFileProxy() {
		IRemoteFileProxy fileProxy = null;
		try {
			fileProxy =  proxyManager.getFileProxy(localProject.getProject());
			assertTrue("Should have returned a remote launcher", fileProxy instanceof LocalFileProxy);
		} catch (CoreException e) {
			fail("Should have returned a launcher: " + e.getCause());
		}

		/*
		 * Test getDirectorySeparator()
		 */
		String ds = fileProxy.getDirectorySeparator();
		assertNotNull(ds);
		/*
		 *  Test getResource()
		 */
		IFileStore actualFileStore = fileProxy.getResource(localProject.getProject().getLocation().toOSString());
		assertNotNull(actualFileStore);

		IFileStore expectedFileStore = null;
		try {
			expectedFileStore = EFS.getStore(localProject.getLocationURI());
		} catch (CoreException e) {
			fail("Unabled to get FileStore to local project: " + e.getMessage());
		}
		assertEquals("FileStore to local project folder diverge", expectedFileStore, actualFileStore);
		assertTrue(actualFileStore.fetchInfo().isDirectory());

		/*
		 * Test getWorkingDir()
		 */
		URI workingDir = fileProxy.getWorkingDir();
		assertNotNull(workingDir);
		assertEquals(localProject.getLocationURI(), workingDir);

		/*
		 * Test toPath()
		 */
		assertEquals(localProject.getProject().getLocation().toOSString(), fileProxy.toPath(localProject.getLocationURI()));
	}
}
