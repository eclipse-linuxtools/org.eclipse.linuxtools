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

import static org.eclipse.linuxtools.docker.core.EnumDockerConnectionSettings.BINDING_MODE;
import static org.eclipse.linuxtools.docker.core.EnumDockerConnectionSettings.TCP_CERT_PATH;
import static org.eclipse.linuxtools.docker.core.EnumDockerConnectionSettings.TCP_HOST;
import static org.eclipse.linuxtools.docker.core.EnumDockerConnectionSettings.TCP_TLS_VERIFY;
import static org.eclipse.linuxtools.docker.core.EnumDockerConnectionSettings.UNIX_SOCKET;
import static org.eclipse.linuxtools.docker.core.EnumDockerConnectionSettings.UNIX_SOCKET_PATH;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.EnumDockerConnectionSettings;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerConnectionInfo;
import org.eclipse.linuxtools.docker.core.IDockerConnectionSettings;
import org.eclipse.linuxtools.docker.core.IDockerConnectionSettingsFinder;
import org.eclipse.linuxtools.docker.core.IDockerConnectionSettingsProvider;

import com.spotify.docker.client.DockerCertificateException;
import com.spotify.docker.client.DockerClient;

/**
 * A utility class that looks for candidate {@link IDockerConnection}s on the
 * host system.
 */
public class DefaultDockerConnectionSettingsFinder
		implements IDockerConnectionSettingsFinder {

	public static final String DOCKER_CERT_PATH = "DOCKER_CERT_PATH"; //$NON-NLS-1$
	public static final String DOCKER_TLS_VERIFY = "DOCKER_TLS_VERIFY"; //$NON-NLS-1$
	public static final String DOCKER_TLS_VERIFY_TRUE = "1"; //$NON-NLS-1$
	public static final String DOCKER_HOST = "DOCKER_HOST"; //$NON-NLS-1$

	@Override
	@Deprecated
	public List<IDockerConnectionSettings> findConnectionSettings() {
		final List<IDockerConnectionSettings> availableConnectionSettings = new ArrayList<>();
		final IDockerConnectionSettings defaultsWithUnixSocket = defaultsWithUnixSocket();
		if (defaultsWithUnixSocket != null) {
			availableConnectionSettings.add(defaultsWithUnixSocket);
		}
		final IDockerConnectionSettings defaultsWithSystemEnv = defaultsWithSystemEnv();
		if (defaultsWithSystemEnv != null) {
			availableConnectionSettings.add(defaultsWithSystemEnv);
		}
		final IDockerConnectionSettings defaultsWithShellEnv = defaultsWithShellEnv();
		if (defaultsWithShellEnv != null) {
			availableConnectionSettings.add(defaultsWithShellEnv);
		}
		// now that we have connection settings, let's ping them and retrieve the connection info.
		for (IDockerConnectionSettings connectionSettings : availableConnectionSettings) {
			switch(connectionSettings.getType()) {
			case UNIX_SOCKET_CONNECTION:
				final UnixSocketConnectionSettings unixSocketConnectionSettings = (UnixSocketConnectionSettings) connectionSettings;
				final DockerConnection unixSocketConnection = new DockerConnection.Builder()
						.unixSocketConnection(unixSocketConnectionSettings);
				resolveDockerName(unixSocketConnectionSettings,
						unixSocketConnection);
				break;
			case TCP_CONNECTION:
				final TCPConnectionSettings tcpConnectionSettings = (TCPConnectionSettings) connectionSettings;
				final DockerConnection tcpConnection = new DockerConnection.Builder()
						.tcpConnection(tcpConnectionSettings);
				resolveDockerName(tcpConnectionSettings, tcpConnection);
				break;
			}
		}
		return availableConnectionSettings;
	}

	@Override
	public IDockerConnectionSettings findDefaultConnectionSettings() {
		final IDockerConnectionSettings defaultsWithUnixSocket = defaultsWithUnixSocket();
		if (defaultsWithUnixSocket != null) {
			return defaultsWithUnixSocket;
		}
		final IDockerConnectionSettings defaultsWithSystemEnv = defaultsWithSystemEnv();
		if (defaultsWithSystemEnv != null) {
			return defaultsWithSystemEnv;
		}
		final IDockerConnectionSettings defaultsWithShellEnv = defaultsWithShellEnv();
		if (defaultsWithShellEnv != null) {
			return defaultsWithShellEnv;
		}
		return null;
	}

	private void resolveDockerName(
			final BaseConnectionSettings connectionSettings,
			final DockerConnection connection) {
		try {
			connection.open(false);
			final IDockerConnectionInfo info = connection.getInfo();
			if (info != null) {
				connection.setName(info.getName());
				connectionSettings.setSettingsResolved(true);
			}
		} catch (DockerException e) {
			// ignore and keep 'settingsResolved' as false
			connectionSettings.setSettingsResolved(false);
		} finally {
			connection.close();
		}
	}

	@Override
	public String resolveConnectionName(
			final IDockerConnectionSettings connectionSettings) {
		if (connectionSettings == null) {
			return null;
		}
		try {
			final DockerClient client = new DockerClientFactory()
					.getClient(connectionSettings);
			if (client != null) {
				return client.info().name();
			}
		} catch (DockerCertificateException
				| com.spotify.docker.client.DockerException
				| InterruptedException e) {
			// ignore and return null
		}
		return null;
	}

	/**
	 * Checks if there is a Unix socket available at the given location
	 * 
	 * @return {@code IDockerConnectionSettings} if the Unix socket exists and
	 *         is readable and writable, {@code null} otherwise.
	 */
	public IDockerConnectionSettings defaultsWithUnixSocket() {
		final List<IDockerConnectionSettings> res = new DefaultUnixConnectionSettingsProvider()
				.getConnectionSettings();
		if (res != null && !res.isEmpty()) {
			return res.get(0);
		}
		return null;
	}

	/**
	 * Checks if there are {@code DOCKER_xxx} environment variables in the
	 * current Eclipse process.
	 * 
	 * @return {@code IDockerConnectionSettings} if the {@code DOCKER_xxx}
	 *         environment variables exist, {@code null} otherwise.
	 */
	public IDockerConnectionSettings defaultsWithSystemEnv() {
		final List<IDockerConnectionSettings> res = new SystemConnectionSettingsProvider()
				.getConnectionSettings();
		if (res != null && !res.isEmpty()) {
			return res.get(0);
		}
		return null;
	}

	/**
	 * Checks if there are {@code DOCKER_xxx} environment variables when running
	 * a script in a shell.
	 * 
	 * @return {@code IDockerConnectionSettings} if the {@code DOCKER_xxx}
	 *         environment variables exist, {@code null} otherwise.
	 */
	public IDockerConnectionSettings defaultsWithShellEnv() {
		final List<IDockerConnectionSettings> res = new ShellConnectionSettingsProvider()
				.getConnectionSettings();
		if (res != null && !res.isEmpty()) {
			return res.get(0);
		}
		return null;
	}

	public static class Defaults {

		public static final String DEFAULT_UNIX_SOCKET_PATH = "unix:///var/run/docker.sock"; //$NON-NLS-1$

		private boolean settingsResolved;
		private String name = null;
		private final Map<EnumDockerConnectionSettings, Object> settings = new HashMap<>();

		public boolean isSettingsResolved() {
			return settingsResolved;
		}

		public String getName() {
			return name;
		}

		/**
		 * @return the default binding mode that was found, or UNIX_SOCKET if
		 *         the property was not was not found.
		 */
		public EnumDockerConnectionSettings getBindingMode() {
			if (settings.containsKey(BINDING_MODE)) {
				return (EnumDockerConnectionSettings) settings
						.get(BINDING_MODE);
			}
			return UNIX_SOCKET;
		}

		/**
		 * @return the path to the Unix socket, or {@code null} if if the
		 *         property was not was not found.
		 */
		public String getUnixSocketPath() {
			return (String) settings.get(UNIX_SOCKET_PATH);
		}

		/**
		 * @return the TCP host, or {@code null} if none was found.
		 */
		public String getTcpHost() {
			return (String) settings.get(TCP_HOST);
		}

		/**
		 * @return the TLS_VERIFY {@link Boolean} flag, or {@code false} if the
		 *         property was not was not found.
		 */
		public boolean getTcpTlsVerify() {
			if (settings.containsKey(TCP_TLS_VERIFY)) {
				return (Boolean) settings.get(TCP_TLS_VERIFY);
			}
			return false;
		}

		/**
		 * @return the path to the TCP certificates, or {@code null} if the
		 *         property was not was found.
		 */
		public String getTcpCertPath() {
			return (String) settings.get(TCP_CERT_PATH);
		}

	}

	@Override
	public List<IDockerConnectionSettings> getKnownConnectionSettings() {
		List<IDockerConnectionSettings> result = new ArrayList<>();
		IConfigurationElement[] configs = getConfigurationElements();
		for (IConfigurationElement config : configs) {
			try {
				Object obj = config.createExecutableExtension("provider"); //$NON-NLS-1$
				if (obj instanceof IDockerConnectionSettingsProvider) {
					List<IDockerConnectionSettings> settings = ((IDockerConnectionSettingsProvider) obj)
							.getConnectionSettings();
					if (settings != null && !settings.isEmpty()) {
						result.addAll(settings);
					}
				}
			} catch (CoreException e) {
				// continue, perhaps another configuration will succeed
			}
		}
		return result;
	}

	/**
	 * Helper method to return the list of extensions that contribute the the
	 * connection registry
	 *
	 * @return All extensions that contribute to the connection registry.
	 */
	private static IConfigurationElement[] getConfigurationElements() {
        IExtensionPoint extPoint = Platform.getExtensionRegistry()
                .getExtensionPoint("org.eclipse.linuxtools.docker.core.connection"); //$NON-NLS-1$
        return extPoint.getConfigurationElements();
    }

}
