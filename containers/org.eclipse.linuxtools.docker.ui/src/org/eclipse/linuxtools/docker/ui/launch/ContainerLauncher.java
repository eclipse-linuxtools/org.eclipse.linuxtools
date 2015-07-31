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
package org.eclipse.linuxtools.docker.ui.launch;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.EnumDockerLoggingStatus;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerContainerExit;
import org.eclipse.linuxtools.docker.core.IDockerContainerInfo;
import org.eclipse.linuxtools.docker.core.IDockerHostConfig;
import org.eclipse.linuxtools.docker.core.IDockerPortBinding;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.linuxtools.internal.docker.core.DockerContainerConfig;
import org.eclipse.linuxtools.internal.docker.core.DockerHostConfig;
import org.eclipse.linuxtools.internal.docker.core.DockerPortBinding;
import org.eclipse.linuxtools.internal.docker.ui.ConsoleOutputStream;
import org.eclipse.linuxtools.internal.docker.ui.RunConsole;
import org.eclipse.linuxtools.internal.docker.ui.views.DVMessages;
import org.eclipse.swt.widgets.Display;

public class ContainerLauncher {

	private static final String ERROR_CREATING_CONTAINER = "ContainerCreateError.msg"; //$NON-NLS-1$
	private static final String ERROR_LAUNCHING_CONTAINER = "ContainerLaunchError.msg"; //$NON-NLS-1$
	private static final String ERROR_NO_CONNECTIONS = "ContainerNoConnections.msg"; //$NON-NLS-1$
	private static final String ERROR_NO_CONNECTION_WITH_URI = "ContainerNoConnectionWithURI.msg"; //$NON-NLS-1$

	private static RunConsole console;

	/**
	 * Perform a launch of a command in a container.
	 * 
	 * @param id
	 *            - id of caller to use to distinguish console owner
	 * @param listener
	 *            - optional listener of the run console
	 * @param connectionUri
	 *            - the specified connection to use
	 * @param image
	 *            - the image to use
	 * @param command
	 *            - command to run
	 * @param commandDir
	 *            - directory command requires or null
	 * @param workingDir
	 *            - working directory or null
	 * @param additionalDirs
	 *            - additional directories to mount or null
	 * @param origEnv
	 *            - original environment if we are appending to our existing
	 *            environment
	 * @param envMap
	 *            - map of environment variable settings
	 * @param ports
	 *            - ports to expose
	 * @param keep
	 *            - keep container after running
	 * @param stdinSupport
	 *            - true if stdin support is required, false otherwise
	 */
	public void launch(String id, IContainerLaunchListener listener,
			final String connectionUri,
			String image, String command, String commandDir, String workingDir,
			List<String> additionalDirs, Map<String, String> origEnv,
			Map<String, String> envMap, List<String> ports, boolean keep,
			boolean stdinSupport) {

		final String LAUNCH_TITLE = "ContainerLaunch.title"; //$NON-NLS-1$
		final String LAUNCH_EXITED_TITLE = "ContainerLaunchExited.title"; //$NON-NLS-1$

		final List<String> env = new ArrayList<>();
		env.addAll(toList(origEnv));
		env.addAll(toList(envMap));

		// Add mounts for any directories we need to run the executable.
		// When we add mount points, we need entries of the form:
		// hostname:mountname.
		// In our case, we want all directories mounted as-is so the executable
		// will
		// run as the user expects.
		final List<String> volumes = new ArrayList<>();
		if (additionalDirs != null) {
			for (String dir : additionalDirs) {
				volumes.add(dir + ":" + dir); //$NON-NLS-1$
			}
		}
		if (workingDir != null)
			volumes.add(workingDir + ":" + workingDir); //$NON-NLS-1$
		if (commandDir != null)
			volumes.add(commandDir + ":" + commandDir); //$NON-NLS-1$

		final List<String> cmdList = getCmdList(command);

		final Set<String> exposedPorts = new HashSet<>();
		final Map<String, List<IDockerPortBinding>> portBindingsMap = new HashMap<>();

		if (ports != null) {
			for (String port : ports) {
				port = port.trim();
				if (port.length() > 0) {
					String[] segments = port.split(":"); //$NON-NLS-1$
					if (segments.length == 1) { // containerPort
						exposedPorts.add(segments[0]);
						portBindingsMap
								.put(segments[0],
										Arrays.asList((IDockerPortBinding) new DockerPortBinding(
												"", ""))); //$NON-NLS-1$ //$NON-NLS-2$
					} else if (segments.length == 2) { // hostPort:containerPort
						exposedPorts.add(segments[1]);
						portBindingsMap
								.put(segments[1],
										Arrays.asList((IDockerPortBinding) new DockerPortBinding(
												"", segments[0]))); //$NON-NLS-1$ //$NON-NLS-2$
					} else if (segments.length == 3) { // either
						// ip:hostPort:containerPort
						// or ip::containerPort
						exposedPorts.add(segments[1]);
						if (segments[1].isEmpty()) {
							portBindingsMap
									.put(segments[2],
											Arrays.asList((IDockerPortBinding) new DockerPortBinding(
													"", segments[0]))); //$NON-NLS-1$ //$NON-NLS-2$
						} else {
							portBindingsMap
									.put(segments[2],
											Arrays.asList((IDockerPortBinding) new DockerPortBinding(
													segments[0], segments[1]))); //$NON-NLS-1$ //$NON-NLS-2$
						}
					}
				}
			}

		}

		// Note we don't pass volumes to the config, but instead we pass them to
		// the
		// HostConfig binds setting

		DockerContainerConfig.Builder builder = new DockerContainerConfig.Builder()
				.openStdin(stdinSupport).env(env).cmd(cmdList).image(image)
				.workingDir(workingDir);
		// add any exposed ports as needed
		if (exposedPorts.size() > 0)
			builder = builder.exposedPorts(exposedPorts);
		final DockerContainerConfig config = builder.build();

		DockerHostConfig.Builder hostBuilder = new DockerHostConfig.Builder()
				.binds(volumes);

		// add any port bindings if specified
		if (portBindingsMap.size() > 0)
			hostBuilder = hostBuilder.portBindings(portBindingsMap);

		final IDockerHostConfig hostConfig = hostBuilder.build();

		final IDockerConnection[] connections = DockerConnectionManager
				.getInstance().getConnections();
		if (connections.length == 0) {
			Display.getDefault().syncExec(new Runnable() {

				@Override
				public void run() {
					MessageDialog.openError(Display.getCurrent()
							.getActiveShell(), DVMessages
							.getString(ERROR_LAUNCHING_CONTAINER), DVMessages
							.getString(ERROR_NO_CONNECTIONS));
				}

			});
			return;
		}

		// Try and use the specified connection that was used before,
		// otherwise, open an error
		int defaultIndex = -1;
		String[] connectionNames = new String[connections.length];
		for (int i = 0; i < connections.length; ++i) {
			connectionNames[i] = connections[i].getName();
			if (connections[i].getUri().equals(connectionUri))
				defaultIndex = i;
		}

		if (defaultIndex == -1) {
			Display.getDefault().syncExec(new Runnable() {

				@Override
				public void run() {
					MessageDialog.openError(
							Display.getCurrent().getActiveShell(),
							DVMessages.getString(ERROR_LAUNCHING_CONTAINER),
							DVMessages.getFormattedString(
									ERROR_NO_CONNECTION_WITH_URI,
									connectionUri));
				}

			});
			return;

		}
		final IDockerConnection connection = connections[defaultIndex];
		final String imageName = image;
		final boolean keepContainer = keep;
		final String consoleId = id;
		final IContainerLaunchListener containerListener = listener;

		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				// create the container
				try {
					String containerId = ((DockerConnection) connection)
							.createContainer(config, hostConfig);
					OutputStream stream = null;
					RunConsole oldConsole = getConsole();
					final RunConsole rc = RunConsole.findConsole(containerId,
							consoleId);
					setConsole(rc);
					rc.clearConsole();
					if (oldConsole != null)
						RunConsole.removeConsole(oldConsole);
					Display.getDefault().syncExec(new Runnable() {

						@Override
						public void run() {
							rc.setTitle(Messages.getFormattedString(
									LAUNCH_TITLE, new String[] {
											cmdList.get(0), imageName }));
						}

					});
					// if (!rc.isAttached()) {
					rc.attachToConsole(connection, containerId);
					// }
					if (rc != null) {
						stream = rc.getOutputStream();
						if (containerListener != null) {
							((ConsoleOutputStream) stream)
									.addConsoleListener(containerListener);
						}
					}
					// Create a unique logging thread id which has container id
					// and console id
					String loggingId = containerId + "." + consoleId;
					((DockerConnection) connection).startContainer(containerId,
							loggingId, stream);
					if (rc != null)
						rc.showConsole();
					if (containerListener != null) {
						IDockerContainerInfo info = ((DockerConnection) connection)
								.getContainerInfo(containerId);
						containerListener.containerInfo(info);
					}
					
					// Wait for the container to finish
					final IDockerContainerExit status = ((DockerConnection) connection)
							.waitForContainer(containerId);
					Display.getDefault().syncExec(new Runnable() {

						@Override
						public void run() {
							rc.setTitle(Messages.getFormattedString(
									LAUNCH_EXITED_TITLE, new String[] {
											status.statusCode().toString(),
											cmdList.get(0), imageName }));
							rc.showConsole();
						}

					});
					
					// Let any container listener know that the container is finished
					if (containerListener != null)
						containerListener.done();
					
					if (!keepContainer) {
						// Drain the logging thread before we remove the
						// container (we need to use the logging id)
						((DockerConnection) connection)
								.stopLoggingThread(loggingId);
						while (((DockerConnection) connection)
								.loggingStatus(loggingId) == EnumDockerLoggingStatus.LOGGING_ACTIVE) {
							Thread.sleep(1000);
						}
						// Look for any Display Log console that the user may
						// have opened which would be
						// separate and make sure it is removed as well
						RunConsole rc2 = RunConsole
								.findConsole(((DockerConnection) connection)
										.getContainer(containerId));
						if (rc2 != null)
							RunConsole.removeConsole(rc2);
						((DockerConnection) connection)
								.removeContainer(containerId);
					}

				} catch (final DockerException e) {
					Display.getDefault().syncExec(new Runnable() {

						@Override
						public void run() {
							MessageDialog.openError(Display.getCurrent()
									.getActiveShell(),
									DVMessages
											.getFormattedString(
													ERROR_CREATING_CONTAINER,
													imageName), e.getMessage());

						}

					});
				} catch (InterruptedException e) {
					// for now
					// do nothing
				}
				((DockerConnection) connection).getContainers(true);
			}

		});
		t.start();
	}

	/**
	 * Clean up the container used for launching
	 * 
	 * @param connectionUri
	 *            the URI of the connection used
	 * @param info
	 *            the container info
	 */
	public void cleanup(String connectionUri, IDockerContainerInfo info) {
		final IDockerConnection[] connections = DockerConnectionManager
				.getInstance().getConnections();
		if (connections.length == 0) {
			return;
		}

		// Try and find the specified connection
		IDockerConnection connection = null;
		for (int i = 0; i < connections.length; ++i) {
			if (connections[i].getUri().equals(connectionUri))
				connection = connections[i];
		}

		if (connection == null) {
			return;
		}

		try {
			connection.killContainer(info.id());
		} catch (DockerException | InterruptedException e) {
			// do nothing
		}
	}

	/**
	 * Get the reusable run console for running C/C++ executables in containers.
	 * 
	 * @return
	 */
	private RunConsole getConsole() {
		// if (console == null) {
		// console = RunConsole.getContainerLessConsole();
		// }
		return console;
	}

	private void setConsole(RunConsole cons) {
		console = cons;
	}

	/**
	 * Take the command string and parse it into a list of strings.
	 * 
	 * @param s
	 * @return list of strings
	 */
	private List<String> getCmdList(String s) {
		ArrayList<String> list = new ArrayList<>();
		int length = s.length();
		boolean insideQuote1 = false; // single-quote
		boolean insideQuote2 = false; // double-quote
		boolean escaped = false;
		StringBuffer buffer = new StringBuffer();
		// Parse the string and break it up into chunks that are
		// separated by white-space or are quoted. Ignore characters
		// that have been escaped, including the escape character.
		for (int i = 0; i < length; ++i) {
			char c = s.charAt(i);
			if (escaped) {
				buffer.append(c);
				escaped = false;
			}
			switch (c) {
			case '\'':
				if (!insideQuote2)
					insideQuote1 = insideQuote1 ^ true;
				else
					buffer.append(c);
				break;
			case '\"':
				if (!insideQuote1)
					insideQuote2 = insideQuote2 ^ true;
				else
					buffer.append(c);
				break;
			case '\\':
				escaped = true;
				break;
			case ' ':
			case '\t':
			case '\r':
			case '\n':
				if (insideQuote1 || insideQuote2)
					buffer.append(c);
				else {
					String item = buffer.toString();
					buffer.setLength(0);
					if (item.length() > 0)
						list.add(item);
				}
				break;
			default:
				buffer.append(c);
				break;
			}
		}
		// add last item of string that will be in the buffer
		String item = buffer.toString();
		if (item.length() > 0)
			list.add(item);
		return list;
	}

	/**
	 * Convert map of environment variables to a {@link List} of KEY=VALUE
	 * String
	 * 
	 * @param variables
	 *            the entries to manipulate
	 * @return the concatenated key/values for each given variable entry
	 */
	private List<String> toList(final Map<String, String> variables) {
		final List<String> result = new ArrayList<>();
		if (variables != null) {
			for (Entry<String, String> entry : variables.entrySet()) {
				final String key = entry.getKey();
				final String value = entry.getValue();

				final String envEntry = key + "=" + value; //$NON-NLS-1$
				result.add(envEntry);
			}
		}
		return result;

	}

}
