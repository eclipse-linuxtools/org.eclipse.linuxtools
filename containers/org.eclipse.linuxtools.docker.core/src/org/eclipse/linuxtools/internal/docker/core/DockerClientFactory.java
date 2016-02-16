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

package org.eclipse.linuxtools.internal.docker.core;

import java.io.File;
import java.net.URI;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DefaultDockerClient.Builder;
import com.spotify.docker.client.DockerCertificateException;
import com.spotify.docker.client.DockerCertificates;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.AuthConfig;

/**
 * Factory for {@link DockerClient}. Decoupling from {@link DockerConnection}
 * implementation to more easily introduce variants (eg, during tests)
 */
public class DockerClientFactory {

	/**
	 * Creates a new {@link DockerClient} configured with the given parameters.
	 * 
	 * @param socketPath
	 *            the path to the Unix Socket
	 * @param tcpHost
	 *            the host name/port for if using a TCP connection
	 * @param tcpCertPath
	 *            the path to the certificates directory if using TLS (or
	 *            <code>null</code> if using an unsecured connection)
	 * @return a new instance of {@link DockerClient}
	 * @throws DockerCertificateException
	 *             if the certificates could not be loaded.
	 */
	public DockerClient getClient(final String socketPath, final String tcpHost,
			final String tcpCertPath) throws DockerCertificateException {
		final boolean validSocketPath = socketPath != null
				&& !socketPath.isEmpty();
		final boolean validTcpHost = tcpHost != null && !tcpHost.isEmpty();
		final boolean validTcpCertPath = tcpCertPath != null
				&& !tcpCertPath.isEmpty();
		Builder builder = DefaultDockerClient.builder();
		try {
			builder.authConfig(AuthConfig.fromDockerConfig().build());
		} catch (Exception e) {
			// AuthConfig can't be found, continue
		}
		if (validSocketPath) {
			return builder.uri(socketPath).build();
		} else if (validTcpCertPath && validTcpHost) {
			return builder.uri(URI.create(tcpHost))
					.dockerCertificates(new DockerCertificates(
							new File(tcpCertPath).toPath()))
					.build();
		} else if (validTcpHost) {
			return builder.uri(URI.create(tcpHost))
					.build();
		}
		return null;
	}
}
