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

package org.eclipse.linuxtools.docker.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.assertj.core.api.Assertions;
import org.eclipse.linuxtools.docker.core.IDockerConnectionSettings.BindingType;
import org.eclipse.linuxtools.internal.docker.core.DefaultDockerConnectionSettingsFinder;
import org.eclipse.linuxtools.internal.docker.core.DefaultDockerConnectionStorageManager;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.linuxtools.internal.docker.core.DockerContainerRefreshManager;
import org.eclipse.linuxtools.internal.docker.core.TCPConnectionSettings;
import org.eclipse.linuxtools.internal.docker.core.UnixSocketConnectionSettings;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerClientFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerConnectionFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerConnectionStorageManagerFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.SWTUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import com.spotify.docker.client.DockerClient;

/**
 *
 */
public class DockerConnectionManagerTest {

	private final DockerConnectionManager dockerConnectionManager = DockerConnectionManager.getInstance();
	private final DockerContainerRefreshManager dockerContainersRefreshManager = DockerContainerRefreshManager
			.getInstance();

	@AfterClass
	public static void restoreDefaultConfig() {
		DockerConnectionManager.getInstance().setConnectionStorageManager(new DefaultDockerConnectionStorageManager());
	}

	@Before
	@After
	public void reset() {
		dockerContainersRefreshManager.reset();
	}

	@Test
	public void shouldRegisterConnectionOnRefreshContainersManager() {
		// given
		final DockerClient client = MockDockerClientFactory.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client)
				.withDefaultTCPConnectionSettings();
		dockerConnectionManager
				.setConnectionStorageManager(MockDockerConnectionStorageManagerFactory.providing(dockerConnection));
		SWTUtils.syncExec(() -> dockerConnectionManager.reloadConnections());
		// when
		dockerConnection.getContainers();
		// then
		Assertions.assertThat(dockerContainersRefreshManager.getConnections()).contains(dockerConnection);
	}

	@Test
	public void shouldUnregisterConnectionOnRefreshContainersManager() {
		// given
		final DockerClient client = MockDockerClientFactory.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client)
				.withDefaultTCPConnectionSettings();
		dockerConnectionManager
				.setConnectionStorageManager(MockDockerConnectionStorageManagerFactory.providing(dockerConnection));
		SWTUtils.syncExec(() -> dockerConnectionManager.reloadConnections());
		dockerConnection.getContainers();
		Assertions.assertThat(dockerContainersRefreshManager.getConnections()).contains(dockerConnection);
		// when
		SWTUtils.syncExec(() -> dockerConnectionManager.removeConnection(dockerConnection));
		SWTUtils.wait(1, TimeUnit.SECONDS);
		// then
		Assertions.assertThat(!dockerContainersRefreshManager.getConnections().contains(dockerConnection));
	}

	@Test
	public void testExtensionPointProvidedConnections() {
		DefaultDockerConnectionSettingsFinder finder = new DefaultDockerConnectionSettingsFinder();
		List<IDockerConnectionSettings> settings = finder.getKnownConnectionSettings();
		assertTrue(settings.size() > 0);
		for (IDockerConnectionSettings s : settings) {
			if (s instanceof TCPConnectionSettings) {
				TCPConnectionSettings t = (TCPConnectionSettings) s;
				assertThat(t.getType()).isEqualTo(BindingType.TCP_CONNECTION);
				assertThat(t.getHost()).isEqualTo("https://1.2.3.4:5678");
				assertThat(t.getPathToCertificates()).isEqualTo("/foo/bar/baz/certs");
				assertThat(t.getName()).isEqualTo("https://1.2.3.4:5678");
			} else if (s instanceof UnixSocketConnectionSettings) {
				UnixSocketConnectionSettings t = (UnixSocketConnectionSettings) s;
				// skip if this is the Linux native or Docker for Mac daemon
				if (t.getPath().equals("unix:///var/run/docker.sock")) {
					continue;
				}
				assertThat(t.getType()).isEqualTo(BindingType.UNIX_SOCKET_CONNECTION);
				assertThat(t.getPath()).isEqualTo("unix:///foo/bar/baz/docker.sock");
				assertThat(t.getName()).isEqualTo("unix:///foo/bar/baz/docker.sock");
			} else {
				fail("Docker Connection Settings does not match a known type");
			}
		}
	}

	public static class UnixTestConnectionProvider implements IDockerConnectionSettingsProvider {
		@Override
		public List<IDockerConnectionSettings> getConnectionSettings() {
			final String socketPath = "unix:///foo/bar/baz/docker.sock";
			UnixSocketConnectionSettings unix = new UnixSocketConnectionSettings(socketPath);
			unix.setName(socketPath);
			return Arrays.asList(new IDockerConnectionSettings[] { unix });
		}
	}

	public static class TCPTestConnectionProvider implements IDockerConnectionSettingsProvider {
		@Override
		public List<IDockerConnectionSettings> getConnectionSettings() {
			final String tcpHost = "https://1.2.3.4:5678";
			final String tcpCertPath = "/foo/bar/baz/certs";
			TCPConnectionSettings tcp = new TCPConnectionSettings(tcpHost, tcpCertPath);
			tcp.setName(tcpHost);
			return Arrays.asList(new IDockerConnectionSettings[] { tcp });
		}
	}
}
