/*******************************************************************************
 * Copyright (c) 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.docker.ui.testutils;

import java.util.Arrays;
import java.util.Collections;

import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.docker.core.IDockerConnectionSettingsFinder;
import org.eclipse.linuxtools.internal.docker.core.TCPConnectionSettings;
import org.eclipse.linuxtools.internal.docker.core.UnixSocketConnectionSettings;
import org.mockito.Mockito;

/**
 * Factory for mocked {@link IDockerConnectionSettingsFinder}
 */
public class MockDockerConnectionSettingsFinder {

	/**
	 * Configures the {@link DockerConnectionManager} singleton to not being
	 * able to detect any connection to Docker daemons.
	 * @return the mocked {@link IDockerConnectionSettingsFinder}
	 */
	public static IDockerConnectionSettingsFinder noDockerConnectionAvailable() {
		final IDockerConnectionSettingsFinder noDockerDaemonAvailable = Mockito
				.mock(IDockerConnectionSettingsFinder.class);
		Mockito.when(noDockerDaemonAvailable.findConnectionSettings()).thenReturn(Collections.emptyList());
		return noDockerDaemonAvailable;
	}

	/**
	 * Configures the {@link DockerConnectionManager} singleton to being
	 * able to detect a <strong>valid Unix Socket</strong> to a Docker daemon.
	 */
	public static void validUnixSocketConnectionAvailable() {
		final IDockerConnectionSettingsFinder validUnixSocketConnectionAvailable = Mockito
				.mock(IDockerConnectionSettingsFinder.class);
		final UnixSocketConnectionSettings unixSocketConnectionSettings = new UnixSocketConnectionSettings("unix://var/run/docker.sock");
		unixSocketConnectionSettings.setName("mock");
		unixSocketConnectionSettings.setSettingsResolved(true);
		Mockito.when(validUnixSocketConnectionAvailable.findConnectionSettings()).thenReturn(Arrays.asList(unixSocketConnectionSettings));
		DockerConnectionManager.getInstance().setConnectionSettingsFinder(validUnixSocketConnectionAvailable);
	}

	/**
	 * Configures the {@link DockerConnectionManager} singleton to being
	 * able to detect a <strong>valid TCP Connection</strong> to a Docker daemon.
	 */
	public static void validTCPConnectionAvailable() {
		final IDockerConnectionSettingsFinder validTCPSocketConnectionAvailable = Mockito
				.mock(IDockerConnectionSettingsFinder.class);
		final TCPConnectionSettings tcpConnectionSettings = new TCPConnectionSettings("tcp://1.2.3.4:1234",true, "/path/to/certs");
		tcpConnectionSettings.setName("mock");
		tcpConnectionSettings.setSettingsResolved(true);
		Mockito.when(validTCPSocketConnectionAvailable.findConnectionSettings()).thenReturn(Arrays.asList(tcpConnectionSettings));
		DockerConnectionManager.getInstance().setConnectionSettingsFinder(validTCPSocketConnectionAvailable);
	}

}
