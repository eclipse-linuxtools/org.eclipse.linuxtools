/*******************************************************************************
 * Copyright (c) 2016 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.docker.core.Activator;
import org.eclipse.linuxtools.docker.core.IDockerConnectionSettings;
import org.eclipse.linuxtools.docker.core.IDockerConnectionSettingsProvider;
import org.eclipse.linuxtools.docker.core.Messages;

public class ShellConnectionSettingsProvider implements IDockerConnectionSettingsProvider {

	@Override
	public List<IDockerConnectionSettings> getConnectionSettings() {
		try {
			final String connectionSettingsDetectionScriptName = getConnectionSettingsDetectionScriptName();
			if (connectionSettingsDetectionScriptName == null) {
				Activator.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
						Messages.Docker_No_Settings_Description_Script));
				return Collections.emptyList();
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
				IDockerConnectionSettings setting = createDockerConnectionSettings(dockerSettings);
				if (setting == null) {
					return Collections.emptyList();
				}
				return Arrays.asList(setting);
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
		return Collections.emptyList();
	}

	/**
	 * @param script
	 *            the script to execute
	 * @return the OS-specific command to run the connection settings detection
	 *         script or <code>null</code> if the current OS is not supported.
	 */
	private String[] getConnectionSettingsDetectionCommandArray(
			final File script) {
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			return new String[] { "cmd.exe", "/C", //$NON-NLS-1$ //$NON-NLS-2$
					script.getAbsolutePath() };
		} else {
			return new String[] { script.getAbsolutePath() };
		}
	}

	/**
	 * Finds the script file in the data directory of the bundle given its name,
	 * or creates it from the 'resources' dir in the bundle if it was not found
	 * in the data dir.
	 * 
	 * @param scriptName
	 *            the name of the script to load in the data dir or in the
	 *            'resources' dir in the bundle
	 * @return the script {@link File}
	 */
	private File getConnectionSettingsDetectionScript(final String scriptName) {
		final File script = Activator.getDefault().getBundle()
				.getDataFile(scriptName);
		// if the script file does not exist or is outdated.
		if (script != null
				&& (!script.exists() || script.lastModified() < Activator
						.getDefault().getBundle().getLastModified())) {
			try (final FileOutputStream output = new FileOutputStream(script);
					final InputStream is = DockerConnection.class
							.getResourceAsStream("/resources/" + scriptName)) { //$NON-NLS-1$
				byte[] buff = new byte[1024];
				int n;
				while ((n = is.read(buff)) > 0) {
					output.write(buff, 0, n);
				}
				script.setExecutable(true);
			} catch (IOException e) {
				Activator.logErrorMessage(ProcessMessages
						.getString("Settings_Detection_Exception"), e); //$NON-NLS-1$
			}
		}
		return script;
	}

	/**
	 * @return the name of the script to run, depending on the OS (Windows, MAc,
	 *         *Nix) or <code>null</code> if the current OS is not supported.
	 */
	private String getConnectionSettingsDetectionScriptName() {
		if (SystemUtils.isLinux()) {
			return "script.sh";//$NON-NLS-1$
		} else if (SystemUtils.isMac()) {
			return "script-macosx.sh";//$NON-NLS-1$
		} else if (SystemUtils.isWindows()) {
			return "script.bat"; //$NON-NLS-1$
		}
		return null;
	}

	private String streamToString(InputStream stream) {
		BufferedReader buff = new BufferedReader(new InputStreamReader(stream));
		StringBuffer res = new StringBuffer();
		String line = ""; //$NON-NLS-1$
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

	/**
	 * Creates connection settings from the given {@code docerSettings}, or
	 * <code>null</code> if the settings did not contain a property with the
	 * {@code DOCKER_HOST} key.
	 * 
	 * @param dockerSettings
	 *            the connection settings
	 * @return the {@link IDockerConnectionSettings} or <code>null</code> if the
	 *         settings are invalid.
	 */
	public static IDockerConnectionSettings createDockerConnectionSettings(
			final Properties dockerSettings) {
		final Object dockerHostEnvVariable = dockerSettings
				.get(DefaultDockerConnectionSettingsFinder.DOCKER_HOST);
		final Object dockerCertPathEnvVariable = dockerSettings
				.get(DefaultDockerConnectionSettingsFinder.DOCKER_CERT_PATH);
		// at least 'dockerHostEnvVariable' should be not null
		if (dockerHostEnvVariable == null) {
			return null;
		}
		return new TCPConnectionSettings(dockerHostEnvVariable.toString(),
				dockerCertPathEnvVariable != null
						? dockerCertPathEnvVariable.toString() : null);
	}

}
