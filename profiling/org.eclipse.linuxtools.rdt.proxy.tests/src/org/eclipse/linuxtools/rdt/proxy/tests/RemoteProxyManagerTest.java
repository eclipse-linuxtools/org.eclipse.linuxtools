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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.internal.profiling.launch.LocalFileProxy;
import org.eclipse.linuxtools.internal.profiling.launch.LocalLauncher;
import org.eclipse.linuxtools.internal.rdt.proxy.Activator;
import org.eclipse.linuxtools.internal.rdt.proxy.RDTCommandLauncher;
import org.eclipse.linuxtools.internal.rdt.proxy.RDTFileProxy;
import org.eclipse.linuxtools.internal.ssh.proxy.SSHCommandLauncher;
import org.eclipse.linuxtools.internal.ssh.proxy.SSHFileProxy;
import org.eclipse.linuxtools.profiling.launch.IRemoteCommandLauncher;
import org.eclipse.linuxtools.profiling.launch.IRemoteFileProxy;
import org.eclipse.linuxtools.remote.proxy.tests.AbstractProxyTest;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteServicesManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests which proxy implementation is chosen for a given URI or project. The
 * remote connection is defined but never opened, as choosing a proxy doesn't
 * need to connect.
 */
@SuppressWarnings("restriction")
public class RemoteProxyManagerTest extends AbstractProxyTest {

	private static final String CONNECTION_TYPE_JSCH = "org.eclipse.remote.JSch";
	private static final String CONNECTION_NAME = "test_connection";
	private static final URI SSH_URI = URI.create("ssh://" + CONNECTION_NAME + "/path/to/file");
	private static final URI JSCH_URI = URI.create("jsch://user@example.invalid:22/path/to/file");
	private static final URI UNSUPPORTED_URI = URI.create("remotetools://MyConnection/path/to/file");

	private IRemoteConnection connection;

	@BeforeEach
	public void createConnection() throws CoreException {
		IRemoteServicesManager manager = Activator.getService(IRemoteServicesManager.class);
		assertNotNull(manager);
		IRemoteConnectionType connectionType = manager.getConnectionType(CONNECTION_TYPE_JSCH);
		assertNotNull(connectionType);
		connection = connectionType.newConnection(CONNECTION_NAME).save();
	}

	@AfterEach
	public void removeConnection() throws CoreException {
		if (connection != null) {
			connection.getConnectionType().removeConnection(connection);
			connection = null;
		}
	}

	@Test
	public void testGetFileProxy() throws CoreException {
		// local URIs and project
		assertInstanceOf(LocalFileProxy.class, proxyManager.getFileProxy(URI.create("/path/to/file")));
		assertInstanceOf(LocalFileProxy.class, proxyManager.getFileProxy(URI.create("file:/path/to/file")));
		assertInstanceOf(LocalFileProxy.class, proxyManager.getFileProxy(localProject.getLocationURI()));
		assertInstanceOf(LocalFileProxy.class, proxyManager.getFileProxy(localProject.getProject()));
		// ssh scheme: both the RDT and the SSH proxy register it, and the first
		// one found in the extension registry is used
		IRemoteFileProxy sshFileProxy = proxyManager.getFileProxy(SSH_URI);
		assertTrue(sshFileProxy instanceof RDTFileProxy || sshFileProxy instanceof SSHFileProxy,
				"Should return a remote file proxy: " + sshFileProxy);
		// jsch scheme
		assertInstanceOf(SSHFileProxy.class, proxyManager.getFileProxy(JSCH_URI));
		// As of org.eclipse.remote 2.0, remotetools scheme is no longer supported
		assertThrows(CoreException.class, () -> proxyManager.getFileProxy(UNSUPPORTED_URI));
	}

	@Test
	public void testGetLauncher() throws CoreException {
		// local URI and project
		assertInstanceOf(LocalLauncher.class, proxyManager.getLauncher(localProject.getLocationURI()));
		assertInstanceOf(LocalLauncher.class, proxyManager.getLauncher(localProject.getProject()));
		// ssh scheme: both the RDT and the SSH proxy register it, and the first
		// one found in the extension registry is used
		IRemoteCommandLauncher sshLauncher = proxyManager.getLauncher(SSH_URI);
		assertTrue(sshLauncher instanceof RDTCommandLauncher || sshLauncher instanceof SSHCommandLauncher,
				"Should return a remote launcher: " + sshLauncher);
		// jsch scheme
		assertInstanceOf(SSHCommandLauncher.class, proxyManager.getLauncher(JSCH_URI));
		// As of org.eclipse.remote 2.0, remotetools scheme is no longer supported
		assertThrows(CoreException.class, () -> proxyManager.getLauncher(UNSUPPORTED_URI));
	}

	@Test
	public void testGetOS() throws CoreException {
		assertFalse(proxyManager.getOS(URI.create("/path/to/file")).isEmpty());
		assertFalse(proxyManager.getOS(URI.create("file:/path/to/file")).isEmpty());
		assertFalse(proxyManager.getOS(localProject.getLocationURI()).isEmpty());
		assertFalse(proxyManager.getOS(localProject.getProject()).isEmpty());
		// As of org.eclipse.remote 2.0, remotetools scheme is no longer supported
		assertThrows(CoreException.class, () -> proxyManager.getOS(UNSUPPORTED_URI));
	}

}
