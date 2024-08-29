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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.linuxtools.internal.profiling.launch.LocalFileProxy;
import org.eclipse.linuxtools.internal.rdt.proxy.RDTFileProxy;
import org.eclipse.linuxtools.profiling.launch.IRemoteFileProxy;
import org.eclipse.linuxtools.remote.proxy.tests.AbstractProxyTest;
import org.eclipse.ptp.rdt.sync.core.SyncConfig;
import org.eclipse.ptp.rdt.sync.core.exceptions.MissingConnectionException;
import org.eclipse.remote.core.IRemoteConnection;
import org.junit.jupiter.api.Test;

@SuppressWarnings("restriction")
public class FileProxyTest extends AbstractProxyTest {

	@Test
	public void testRemoteFileProxyOnSyncProject() {
		IRemoteFileProxy fileProxy = null;
		try {
			fileProxy =  proxyManager.getFileProxy(syncProject.getProject());
			assertInstanceOf(RDTFileProxy.class, fileProxy, "Should have returned a remote launcher");
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
		assertEquals(connScheme, fs.toURI().getScheme(), "Remote connection and FileStore schemes diverge");
		//assertTrue(fs.fetchInfo().isDirectory());

		fs = fileProxy.getResource("/filenotexits");
		assertNotNull(fs);
		IFileInfo fileInfo = fs.fetchInfo();
		assertNotNull(fileInfo);
		assertFalse(fileInfo.exists());

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

		/*
		 * Test it opens connection
		 */
		assertNotNull(conn);
		conn.close();
		assertFalse(conn.isOpen());
		try {
			fileProxy =  proxyManager.getFileProxy(syncProject.getProject());
			assertNotNull(fileProxy);
		} catch (CoreException e) {
			fail("Failed to obtain file proxy when connection is closed: " + e.getMessage());
		}
		fs = fileProxy.getResource("/tmp/somedir");
		assertNotNull(fs);
		assertFalse(fs.fetchInfo().exists());
		try {
			fs.mkdir(EFS.SHALLOW, new NullProgressMonitor());
		} catch (CoreException e) {
			fail("should be able to create a directory when connection is closed: " + e.getMessage());
		}
		assertTrue(fs.fetchInfo().exists());
		try {
			fs.delete(EFS.NONE, new NullProgressMonitor());
		} catch (CoreException e) {
			fail("Failed to delete file: " + e.getMessage());
		}
	}

	@Test
	public void testLocalFileProxy() {
		IRemoteFileProxy fileProxy = null;
		try {
			fileProxy =  proxyManager.getFileProxy(localProject.getProject());
			assertInstanceOf(LocalFileProxy.class, fileProxy, "Should have returned a remote launcher");
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
		assertEquals(expectedFileStore, actualFileStore, "FileStore to local project folder diverge");
		assertTrue(actualFileStore.fetchInfo().isDirectory());

		actualFileStore = fileProxy.getResource("/filenotexits");
		assertNotNull(actualFileStore);
		IFileInfo fileInfo = actualFileStore.fetchInfo();
		assertNotNull(fileInfo);
		assertFalse(fileInfo.exists());

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
