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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.docker.core.Activator;
import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.EnumDockerConnectionSettings;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerConnectionInfo;
import org.eclipse.linuxtools.docker.core.IDockerConnectionSettings;
import org.eclipse.linuxtools.docker.core.IDockerConnectionSettingsFinder;
import org.eclipse.linuxtools.docker.core.Messages;

import jnr.unixsocket.UnixSocketAddress;
import jnr.unixsocket.UnixSocketChannel;

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
						.unixSocket(unixSocketConnectionSettings.getPath())
						.build();
				resolveDockerName(unixSocketConnectionSettings,
						unixSocketConnection);
				break;
			case TCP_CONNECTION:
				final TCPConnectionSettings tcpConnectionSettings = (TCPConnectionSettings) connectionSettings;
				final DockerConnection tcpConnection = new DockerConnection.Builder()
						.tcpHost(tcpConnectionSettings.getHost())
						.tcpCertPath(
								tcpConnectionSettings.getPathToCertificates())
						.build();
				resolveDockerName(tcpConnectionSettings, tcpConnection);
				break;
			}
		}
		return availableConnectionSettings;
	}

	private void resolveDockerName(
			final BaseConnectionSettings connectionSettings,
			final DockerConnection connection) {
		try {
			connection.open(false);
			final IDockerConnectionInfo info = connection.getInfo();
			if (info != null) {
				connectionSettings.setName(info.getName());
				connectionSettings.setSettingsResolved(true);
			}
		} catch (DockerException e) {
			// ignore and keep 'settingsResolved' to false
			connectionSettings.setSettingsResolved(false);
		} finally {
			connection.close();
		}
	}

	/**
	 * Checks if there is a Unix socket available at the given location
	 * 
	 * @return {@code IDockerConnectionSettings} if the Unix socket exists and
	 *         is readable and writable, {@code null} otherwise.
	 */
	private IDockerConnectionSettings defaultsWithUnixSocket() {
		final File unixSocketFile = new File("/var/run/docker.sock"); //$NON-NLS-1$
		if (unixSocketFile.exists() && unixSocketFile.canRead()
				&& unixSocketFile.canWrite()) {
			final UnixSocketAddress address = new UnixSocketAddress(
					unixSocketFile);
			try (final UnixSocketChannel channel = UnixSocketChannel
					.open(address)) {
				// assume socket works
				return new UnixSocketConnectionSettings(
						unixSocketFile.getAbsolutePath());
			} catch (IOException e) {
				// do nothing, just assume socket did not work.
			}
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
	private IDockerConnectionSettings defaultsWithSystemEnv() {
		final String dockerHostEnv = System.getenv(DOCKER_HOST);
		if (dockerHostEnv != null) {
			final String tlsVerifyEnv = System.getenv(DOCKER_TLS_VERIFY);
			final boolean useTls = tlsVerifyEnv != null
					&& tlsVerifyEnv.equals(DOCKER_TLS_VERIFY_TRUE); // $NON-NLS-1$
			final String pathToCertificates = System.getenv(DOCKER_CERT_PATH);
			return new TCPConnectionSettings(dockerHostEnv, useTls,
					pathToCertificates);
		}
		return null;
	}

	/**
	 * Checks if there are {@code DOCKER_xxx} environment variables when running
	 * a script in a shell.
	 * 
	 * @return {@code IDockerConnectionSettings} if the {@code DOCKER_xxx}
	 *         environment variables exist, {@code null} otherwise.
	 * @throws DockerException
	 */
	private IDockerConnectionSettings defaultsWithShellEnv() {
		try {
			final String connectionSettingsDetectionScriptName = getConnectionSettingsDetectionScriptName();
			if (connectionSettingsDetectionScriptName == null) {
				Activator.log(new Status(IStatus.WARNING, Activator.PLUGIN_ID,
						Messages.Docker_No_Settings_Description_Script));
				return null;
			}
			final File connectionSettingsDetectionScript = getConnectionSettingsDetectionScript(
					connectionSettingsDetectionScriptName);
			final String[] cmdArray = getConnectionSettingsDetectionCommandArray(
					connectionSettingsDetectionScript);
			final Process process = Runtime.getRuntime().exec(cmdArray);
			process.waitFor();
			final int exitValue = process.exitValue();
			if (exitValue == 0) {
				final InputStream processInputStream = process.getInputStream();
				// read content from process input stream
				final Properties dockerSettings = new Properties();
				dockerSettings.load(processInputStream);
				final Object dockerHostEnvVariable = dockerSettings.get(DOCKER_HOST);
				final Object dockerTlsVerifyEnvVariable = dockerSettings
						.get(DOCKER_TLS_VERIFY);
				final Object dockerCertPathEnvVariable = dockerSettings
						.get(DOCKER_CERT_PATH);
				return new TCPConnectionSettings(
						dockerHostEnvVariable != null
								? dockerHostEnvVariable.toString() : null,
						dockerTlsVerifyEnvVariable != null
								? dockerTlsVerifyEnvVariable
										.equals(DOCKER_TLS_VERIFY_TRUE)
								: null,
						dockerCertPathEnvVariable != null
								? dockerCertPathEnvVariable.toString() : null);
			} else {
				// log what happened if the process did not end as expected
				// an exit value of 1 should indicate no connection found
				if (exitValue != 1) {
					final InputStream processErrorStream = process
							.getErrorStream();
					final String errorMessage = streamToString(
							processErrorStream);
					Activator.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
							errorMessage));
				}
			}
		} catch (IOException | IllegalArgumentException
				| InterruptedException e) {
			Activator.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
					Messages.Retrieve_Default_Settings_Failure, e));
		}
		return null;
	}

	/**
	 * @param script
	 *            the script to execute
	 * @return the OS-specific command to run the connection settings
	 *         detection script.
	 */
	private String[] getConnectionSettingsDetectionCommandArray(
			final File script) {
		final String osName = System.getProperty("os.name"); //$NON-NLS-1$
		if (osName.toLowerCase().startsWith("win")) { //$NON-NLS-1$
			return new String[] { "cmd.exe", "/C", //$NON-NLS-1$ //$NON-NLS-2$
					script.getAbsolutePath() };
		} else if (osName.toLowerCase().startsWith("mac") //$NON-NLS-1$
				|| osName.toLowerCase().contains("linux") //$NON-NLS-1$
				|| osName.toLowerCase().contains("nix")) { //$NON-NLS-1$
			return new String[] { script.getAbsolutePath() };
		} else {
			return null;
		}
	}

	/**
	 * Finds the script file in the data directory of the bundle given its
	 * name, or creates it from the 'resources' dir in the bundle if it was
	 * not found in the data dir.
	 * 
	 * @param scriptName
	 *            the name of the script to load in the data dir or in the
	 *            'resources' dir in the bundle
	 * @return the script {@link File}
	 */
	private File getConnectionSettingsDetectionScript(
			final String scriptName) {
		final File script = Activator.getDefault().getBundle()
				.getDataFile(scriptName);
		// if the script file does not exist or is outdated.
		if (script != null
				&& (!script.exists() || script.lastModified() < Activator
						.getDefault().getBundle().getLastModified())) {
			try (final FileOutputStream output = new FileOutputStream(
					script);
					final InputStream is = DockerConnection.class
							.getResourceAsStream(
									"/resources/" + scriptName)) { //$NON-NLS-1$
				byte[] buff = new byte[1024];
				int n;
				while ((n = is.read(buff)) > 0) {
					output.write(buff, 0, n);
				}
				script.setExecutable(true);
			} catch (IOException e) {
				Activator.logErrorMessage(e.getMessage());
			}
		}
		return script;
	}

	/**
	 * @return the name of the script to run, depending on the OS (Windows,
	 *         MAc, *Nix)
	 */
	private String getConnectionSettingsDetectionScriptName() {
		final String osName = System.getProperty("os.name"); //$NON-NLS-1$
		if (osName.toLowerCase().startsWith("win")) { //$NON-NLS-1$
			return "script.bat"; //$NON-NLS-1$
		} else if (osName.toLowerCase().startsWith("mac") //$NON-NLS-1$
				|| osName.toLowerCase().contains("linux") //$NON-NLS-1$
				|| osName.toLowerCase().contains("nix")) { //$NON-NLS-1$
			return "script.sh";//$NON-NLS-1$
		} else {
			return null;
		}
	}

	private String streamToString(InputStream stream) {
		BufferedReader buff = new BufferedReader(
				new InputStreamReader(stream));
		StringBuffer res = new StringBuffer();
		String line = "";
		try {
			while ((line = buff.readLine()) != null) {
				res.append(System.getProperty("line.separator")); //$NON-NLS-1$
				res.append(line);
			}
			buff.close();
		} catch (IOException e) {
		}
		return res.length() > 0 ? res.substring(1) : "";
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

}
