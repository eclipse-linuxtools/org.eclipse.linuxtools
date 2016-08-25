/*******************************************************************************
 * Copyright (c) 2015, 2016 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.docker.ui.testutils;

import java.util.Collections;

import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.docker.core.IDockerConnectionSettings;
import org.eclipse.linuxtools.docker.core.IDockerConnectionSettingsFinder;
import org.eclipse.linuxtools.internal.docker.core.TCPConnectionSettings;
import org.eclipse.linuxtools.internal.docker.core.UnixSocketConnectionSettings;
import org.mockito.Matchers;
import org.mockito.Mockito;

/**
 * Factory for mocked {@link IDockerConnectionSettingsFinder}
 */
public class MockDockerConnectionSettingsFinder {

	/**
	 * Configures the {@link DockerConnectionManager} singleton to not being
	 * able to detect any connection to Docker daemons.
	 */
	public static void noDockerConnectionAvailable() {
		final IDockerConnectionSettingsFinder noDockerDaemonAvailable = Mockito
				.mock(IDockerConnectionSettingsFinder.class);
		Mockito.when(noDockerDaemonAvailable.findConnectionSettings()).thenReturn(Collections.emptyList());
		DockerConnectionManager.getInstance().setConnectionSettingsFinder(noDockerDaemonAvailable);
	}

	/**
	 * Configures the {@link DockerConnectionManager} singleton to being able to
	 * detect a <strong>valid Unix Socket</strong> to a Docker daemon.
	 */
	public static void validUnixSocketConnectionAvailable() {
		validUnixSocketConnectionAvailable("mock", "unix:///var/run/docker.sock");
	}

	/**
	 * Configures the {@link DockerConnectionManager} singleton to being able to
	 * detect a <strong>valid Unix Socket</strong> to a Docker daemon.
	 */
	public static void validUnixSocketConnectionAvailable(final String connectionName, final String unixSocketPath) {
		final IDockerConnectionSettingsFinder validUnixSocketConnectionAvailable = Mockito
				.mock(IDockerConnectionSettingsFinder.class);
		final UnixSocketConnectionSettings unixSocketConnectionSettings = new UnixSocketConnectionSettings(
				unixSocketPath);
		//unixSocketConnectionSettings.setName("mock");
		unixSocketConnectionSettings.setSettingsResolved(true);
		Mockito.when(validUnixSocketConnectionAvailable.findDefaultConnectionSettings()).thenReturn(unixSocketConnectionSettings);
		Mockito.when(
				validUnixSocketConnectionAvailable.resolveConnectionName(Matchers.any(IDockerConnectionSettings.class)))
				.thenReturn(connectionName);
		DockerConnectionManager.getInstance().setConnectionSettingsFinder(validUnixSocketConnectionAvailable);
	}

	/**
	 * Configures the {@link DockerConnectionManager} singleton to being
	 * able to detect a <strong>valid TCP Connection</strong> to a Docker daemon.
	 */
	public static void validTCPConnectionAvailable() {
		validTCPConnectionAvailable("mock", "tcp://1.2.3.4:1234", "/path/to/certs");
	}

	public static void validTCPConnectionAvailable(final String connectionName, final String host,
			final String pathToCerts) {

		final IDockerConnectionSettingsFinder validTCPSocketConnectionAvailable = Mockito
				.mock(IDockerConnectionSettingsFinder.class);
		final TCPConnectionSettings tcpConnectionSettings = new TCPConnectionSettings(host, pathToCerts);
		//tcpConnectionSettings.setName("mock");
		tcpConnectionSettings.setSettingsResolved(true);
		Mockito.when(validTCPSocketConnectionAvailable.findDefaultConnectionSettings()).thenReturn(tcpConnectionSettings);
		Mockito.when(
				validTCPSocketConnectionAvailable.resolveConnectionName(Matchers.any(IDockerConnectionSettings.class)))
				.thenReturn(connectionName);
		DockerConnectionManager.getInstance().setConnectionSettingsFinder(validTCPSocketConnectionAvailable);
	}

}
