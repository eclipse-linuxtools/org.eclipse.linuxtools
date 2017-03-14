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

import org.eclipse.linuxtools.docker.core.EnumDockerConnectionState;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerImageInfo;
import org.eclipse.linuxtools.internal.docker.core.DockerClientFactory;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.linuxtools.internal.docker.core.TCPConnectionSettings;
import org.eclipse.linuxtools.internal.docker.core.UnixSocketConnectionSettings;
import org.mockito.Matchers;
import org.mockito.Mockito;

import com.spotify.docker.client.DockerCertificateException;
import com.spotify.docker.client.DockerClient;

/**
 * Factory for mocked {@link IDockerConnection}
 */
public class MockDockerConnectionFactory {

	public static Builder from(final String name, final DockerClient dockerClient) {
		return new Builder(name, dockerClient);
	}

	public static class Builder {

		private final DockerConnection.Builder connectionBuilder;

		private final DockerClient dockerClient;

		private Builder(final String name, final DockerClient dockerClient) {
			this.connectionBuilder = new DockerConnection.Builder().name(name);
			this.dockerClient = dockerClient;
		}

		public DockerConnection withUnixSocketConnectionSettings(final String pathToSocket) {
			final DockerConnection connection = Mockito.spy(connectionBuilder.unixSocketConnection(new UnixSocketConnectionSettings(pathToSocket)));
			configureDockerClientFactory(connection, this.dockerClient);
			configureMockBehaviour(connection);
			return connection;
		}

		public DockerConnection withDefaultTCPConnectionSettings() {
			return withTCPConnectionSettings(null, null);
		}

		public DockerConnection withTCPConnectionSettings(final String host, final String pathToCerts) {
			final DockerConnection connection = Mockito.spy(connectionBuilder.tcpConnection(new TCPConnectionSettings(host, pathToCerts)));
			configureDockerClientFactory(connection, this.dockerClient);
			configureMockBehaviour(connection);
			return connection;
		}

		public DockerConnection withState(final EnumDockerConnectionState state) {
			final DockerConnection connection = withDefaultTCPConnectionSettings();
			Mockito.when(connection.getState()).thenReturn(state);
			return connection;
		}

		private static void configureMockBehaviour(final DockerConnection connection) {
			final IDockerImageInfo imageInfo = Mockito.mock(IDockerImageInfo.class, Mockito.RETURNS_DEEP_STUBS);
			Mockito.when(connection.getImageInfo(Matchers.anyString())).thenReturn(imageInfo);
			// Mockito.when(connection.isOpen()).thenReturn(state ==
			// EnumDockerConnectionState.ESTABLISHED);
		}

		private static void configureDockerClientFactory(final DockerConnection connection, final DockerClient dockerClient) {
			final DockerClientFactory dockerClientFactory = Mockito.mock(DockerClientFactory.class);
			connection.setDockerClientFactory(dockerClientFactory);
			try {
				// return dockerClient without auth
				Mockito.when(dockerClientFactory.getClient(Matchers.any())).thenReturn(dockerClient);
				// return same dockerClient with auth arg
				Mockito.when(dockerClientFactory.getClient(Matchers.any(), Matchers.any())).thenReturn(dockerClient);
			} catch (DockerCertificateException e) {
				// rest assured, nothing will happen while mocking the DockerClientFactory
			}
		}


	}

}
