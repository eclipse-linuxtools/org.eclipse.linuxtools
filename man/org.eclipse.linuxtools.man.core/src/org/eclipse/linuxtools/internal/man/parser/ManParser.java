/*******************************************************************************
 * Copyright (c) 2009, 2018 Red Hat Inc. and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alexander Kurtakov - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.man.parser;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.linuxtools.internal.man.preferences.PreferenceConstants;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * Parser for the man executable output.
 */
public class ManParser {

	private static final int DEFAULT_SSH_PORT = 22;

	/**
	 * Gets the list of paths returned when one runs "man -w" with no other
	 * parameters. This is the list of directories that is searched by man for
	 * man pages.
	 * 
	 * @return the list of paths in which man searches for man pages in same
	 *         order that man would return them
	 */
	public static List<Path> getManPaths() {
		// Build param list
		List<String> params = new ArrayList<>();
		params.add(getManExecutable());
		params.add("-w"); //$NON-NLS-1$

		List<Path> manPaths = new ArrayList<>();
		ProcessBuilder builder = new ProcessBuilder(params);
		try (InputStream stdout = builder.start().getInputStream()) {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			int x;
			while ((x = stdout.read()) != -1) {
				bos.write(x);
			}
			for (String path : bos.toString().trim()
					.split(File.pathSeparator)) {
				manPaths.add(Paths.get(path));
			}
		} catch (IOException e) {
			Bundle bundle = FrameworkUtil.getBundle(ManParser.class);
			IStatus status = Status.error(e.getMessage());
			Platform.getLog(bundle).log(status);
		}
		return manPaths;
	}

	/**
	 * Opens a manual page and returns an input stream from which to read it.
	 * 
	 * @param page
	 *            the name of the man page to open
	 * @param html
	 *            true to open the given man page as an HTML document, false to
	 *            open it as a plain text document suitable for display in a
	 *            terminal
	 * @param sections
	 *            a string array of manual sections in which to look for the
	 *            given man page
	 * @return a new input stream, the caller is responsible for closing it
	 */
	public InputStream getManPage(String page, boolean html,
			String... sections) {
		StringBuilder sectionParam = new StringBuilder();
		for (String section : sections) {
			if (sectionParam.length() > 0) {
				sectionParam.append(':');
			}
			sectionParam.append(section);
		}

		// Build param list
		List<String> params = new ArrayList<>();
		params.add(getManExecutable());
		if (page != null && !page.isEmpty() && sectionParam.length() > 0) {
			params.add("-S"); //$NON-NLS-1$
			params.add(sectionParam.toString());
		}
		if (html) {
			params.add("-Thtml"); //$NON-NLS-1$
		}
		params.add(page);

		ProcessBuilder builder = new ProcessBuilder(params);
		InputStream stdout = null;
		try {
			Process process = builder.start();
			stdout = process.getInputStream();
		} catch (IOException e) {
			Bundle bundle = FrameworkUtil.getBundle(this.getClass());
			Platform.getLog(bundle).log(Status.error(e.getMessage()));
		}
		return stdout;
	}

	/**
	 * Returns the raw representation of the man executable for a given man page
	 * i.e. `man ls`.
	 *
	 * @param manPage
	 *            The man page to fetch.
	 * @return Raw output of the man command.
	 */
	public StringBuilder getRawManPage(String manPage) {
		StringBuilder sb = new StringBuilder();
		try (InputStream manContent = getManPage(manPage, false);
				BufferedReader reader = manContent != null
						? new BufferedReader(new InputStreamReader(manContent))
						: null) {
			if (reader != null) {
				sb.append(reader.lines().collect(Collectors.joining("\n"))); //$NON-NLS-1$
			}
		} catch (IOException e) {
			Bundle bundle = FrameworkUtil.getBundle(this.getClass());
			Platform.getLog(bundle).log(Status.error(e.getMessage()));
		}
		return sb;
	}

	/**
	 * Returns the raw representation of the man page of an executable on a
	 * remote machine.
	 *
	 * @param manPage
	 *            The man page to fetch.
	 * @param user
	 *            The name of the user to access the man page as.
	 * @param host
	 *            The name of host where the man page is to be fetched from.
	 * @param password
	 *            The user's login password.
	 * @return Raw output of the man command.
	 */
	public StringBuilder getRemoteRawManPage(String manPage, String user,
			String host, String password) {
		final StringBuilder sb = new StringBuilder();
		OutputStream out = new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				sb.append((char) b);
			}
		};
		try {
			execRemoteAndWait(new String[] { getManExecutable(), manPage }, out,
					out, user, host, password);
		} catch (JSchException e) {
			sb.setLength(0);
			sb.append(Messages.ManParser_RemoteAccessError);
		}
		return sb;
	}

	private static Channel execRemoteAndWait(String[] args, OutputStream out,
			OutputStream err, String user, String host, String password)
			throws JSchException {
		Channel channel = execRemote(args, out, err, user, host, password);

		while (!channel.isClosed()) {
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
				// Thread was interrupted just return.
				return channel;
			}
		}

		return channel;
	}

	private static Channel execRemote(String[] args, OutputStream out,
			OutputStream err, String user, String host, String password)
			throws JSchException {
		JSch jsch = new JSch();
		Session session = jsch.getSession(user, host, DEFAULT_SSH_PORT);
		session.setPassword(password);
		Properties config = new Properties();
		config.put("StrictHostKeyChecking", "no"); //$NON-NLS-1$//$NON-NLS-2$
		session.setConfig(config);
		session.connect();

		StringBuilder command = new StringBuilder();
		for (int i = 0; i < args.length; i++) {
			command.append(args[i] + ' ');
		}

		ChannelExec channel = (ChannelExec) session.openChannel("exec"); //$NON-NLS-1$
		channel.setPty(true);
		channel.setCommand(command.toString());
		channel.setInputStream(null, true);
		channel.setOutputStream(out, true);
		channel.setExtOutputStream(err, true);
		channel.connect();

		return channel;
	}

	private static String getManExecutable() {
		IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(
				FrameworkUtil.getBundle(ManParser.class).getSymbolicName());
		return prefs.get(PreferenceConstants.P_PATH,
				PreferenceConstants.P_PATH_DEFAULT);
	}
}
