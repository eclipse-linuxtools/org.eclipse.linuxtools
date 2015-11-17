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
import com.spotify.docker.client.DockerCertificateException;
import com.spotify.docker.client.DockerCertificates;
import com.spotify.docker.client.DockerClient;

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
		if (socketPath != null) {
			return DefaultDockerClient.builder().uri(socketPath).build();
		} else if (tcpCertPath != null) {
			return DefaultDockerClient.builder().uri(URI.create(tcpHost))
					.dockerCertificates(new DockerCertificates(
							new File(tcpCertPath).toPath()))
					.build();
		} else {
			return DefaultDockerClient.builder().uri(URI.create(tcpHost))
					.build();
		}

	}
}
