/*******************************************************************************
 * Copyright (c) 2015, 2023 Red Hat.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.docker.core;

import static java.util.Collections.singletonMap;

import java.io.File;
import java.net.URI;
import java.util.Objects;

import org.eclipse.linuxtools.docker.core.IDockerConnectionSettings;
import org.eclipse.linuxtools.docker.core.IDockerConnectionSettings.BindingType;
import org.eclipse.linuxtools.docker.core.IRegistryAccount;
import org.mandas.docker.client.DefaultDockerClient;
import org.mandas.docker.client.DockerCertificates;
import org.mandas.docker.client.DockerClient;
import org.mandas.docker.client.auth.FixedRegistryAuthSupplier;
import org.mandas.docker.client.builder.jersey.JerseyDockerClientBuilder;
import org.mandas.docker.client.exceptions.DockerCertificateException;
import org.mandas.docker.client.messages.RegistryAuth;
import org.mandas.docker.client.messages.RegistryConfigs;

/**
 * Factory for {@link DockerClient}. Decoupling from {@link DockerConnection}
 * implementation to more easily introduce variants (eg, during tests)
 */
public class DockerClientFactory {

	/**
	 * Creates a new {@link DockerClient} from the given
	 * {@link IDockerConnectionSettings}.
	 *
	 * @param connectionSettings
	 *            the connection settings
	 * @return the {@link DockerClient}
	 * @throws DockerCertificateException
	 *             if the path to Docker certificates is invalid (missing files)
	 */
	public DockerClient getClient(
			final IDockerConnectionSettings connectionSettings)
			throws DockerCertificateException {
		return getClient(connectionSettings, null);
	}

	/**
	 * Creates a new {@link DockerClient} from the given
	 * {@link IDockerConnectionSettings}.
	 *
	 * @param connectionSettings
	 *            the connection settings
	 * @return the {@link DockerClient} or <code>null</code> if the connection
	 *         URI (Unix socker path or TCP host) was missing (ie,
	 *         <code>null</code> or empty)
	 * @throws DockerCertificateException
	 *             if the path to Docker certificates is invalid (missing files)
	 */
	public DockerClient getClient(
			final IDockerConnectionSettings connectionSettings,
			final IRegistryAccount registryAccount)
			throws DockerCertificateException {
		final JerseyDockerClientBuilder builder = new JerseyDockerClientBuilder();
		if (connectionSettings
				.getType() == BindingType.UNIX_SOCKET_CONNECTION) {
			final UnixSocketConnectionSettings unixSocketConnectionSettings = (UnixSocketConnectionSettings) connectionSettings;
			if (unixSocketConnectionSettings.hasPath()) {
				builder.uri(unixSocketConnectionSettings.getPath());
			}

		} else {
			final TCPConnectionSettings tcpConnectionSettings = (TCPConnectionSettings) connectionSettings;
			if (tcpConnectionSettings.hasHost()) {
				builder.uri(URI.create(tcpConnectionSettings.getHost()));
				if (tcpConnectionSettings.getPathToCertificates() != null
						&& !tcpConnectionSettings.getPathToCertificates()
								.isEmpty()) {
					builder.dockerCertificates(new DockerCertificates(new File(
							tcpConnectionSettings.getPathToCertificates())
									.toPath()));
				}
			}
		}
		// skip if no URI exists
		if (builder.uri() == null) {
			return null;
		}

		if (registryAccount != null) {
			// mimic spotify:
			// https://github.com/spotify/docker-client/blob/master/src/main/java/com/spotify/docker/client/DefaultDockerClient.java#L3140
			RegistryAuth registryAuth = buildAuthentication(registryAccount);
			final RegistryConfigs configs = RegistryConfigs.create(singletonMap(
					Objects.toString(registryAuth.serverAddress(), ""),
					registryAuth));
			builder.registryAuthSupplier(
					new FixedRegistryAuthSupplier(registryAuth, configs));
		}
		// TODO This is giant hack to make jersey ClientBuilder initialized
		// Should be removed before merging
		final String clientBuilderProperty = "jakarta.ws.rs.client.ClientBuilder"; //$NON-NLS-1$
		String originalClientBuilder = System.setProperty(clientBuilderProperty,
				"org.glassfish.jersey.client.JerseyClientBuilder"); //$NON-NLS-1$
		final String runtimeDelegateProperty = "jakarta.ws.rs.ext.RuntimeDelegate"; //$NON-NLS-1$
		String originalRuntimeDelegate = System.setProperty(
				runtimeDelegateProperty,
				"org.glassfish.jersey.internal.RuntimeDelegateImpl"); //$NON-NLS-1$
		DefaultDockerClient dockerClient = builder.build();
		if (originalClientBuilder == null) {
			System.clearProperty(clientBuilderProperty);
		} else {
			System.setProperty(clientBuilderProperty, originalClientBuilder);
		}
		if (originalRuntimeDelegate == null) {
			System.clearProperty(runtimeDelegateProperty);
		} else {
			System.setProperty(runtimeDelegateProperty,
					originalRuntimeDelegate);
		}
		return dockerClient;
	}

	private RegistryAuth buildAuthentication(final IRegistryAccount info) {
		if (info.getUsername() != null && !info.getUsername().isEmpty()) {
			final RegistryAuth authAccount = RegistryAuth.builder()
					.serverAddress(info.getServerAddress())
					.username(info.getUsername()).email(info.getEmail())
					.password(info.getPassword() != null
							? new String(info.getPassword()) : null)
					.build();
			return authAccount;
		}
		return null;
	}

}
