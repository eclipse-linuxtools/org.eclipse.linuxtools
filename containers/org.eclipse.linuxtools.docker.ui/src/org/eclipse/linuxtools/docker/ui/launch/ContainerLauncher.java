/*******************************************************************************
 * Copyright (c) 2015, 2023 Red Hat Inc. and others.
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
package org.eclipse.linuxtools.docker.ui.launch;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerContainerConfig;
import org.eclipse.linuxtools.docker.core.IDockerContainerExit;
import org.eclipse.linuxtools.docker.core.IDockerContainerInfo;
import org.eclipse.linuxtools.docker.core.IDockerHostConfig;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.docker.core.IDockerImageInfo;
import org.eclipse.linuxtools.docker.core.IDockerPortBinding;
import org.eclipse.linuxtools.docker.core.IDockerVolume;
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.linuxtools.internal.docker.core.DockerConsoleOutputStream;
import org.eclipse.linuxtools.internal.docker.core.DockerContainerConfig;
import org.eclipse.linuxtools.internal.docker.core.DockerHostConfig;
import org.eclipse.linuxtools.internal.docker.core.DockerPortBinding;
import org.eclipse.linuxtools.internal.docker.core.IConsoleListener;
import org.eclipse.linuxtools.internal.docker.ui.consoles.ConsoleOutputStream;
import org.eclipse.linuxtools.internal.docker.ui.consoles.RunConsole;
import org.eclipse.linuxtools.internal.docker.ui.jobs.CopyFromDockerJob;
import org.eclipse.linuxtools.internal.docker.ui.jobs.CopyFromDockerJob.CopyType;
import org.eclipse.linuxtools.internal.docker.ui.launch.ContainerCommandProcess;
import org.eclipse.linuxtools.internal.docker.ui.views.DVMessages;
import org.eclipse.linuxtools.internal.docker.ui.wizards.DataVolumeModel;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

public class ContainerLauncher {

	private static final String ERROR_CREATING_CONTAINER = "ContainerCreateError.msg"; //$NON-NLS-1$
	private static final String ERROR_LAUNCHING_CONTAINER = "ContainerLaunchError.msg"; //$NON-NLS-1$
	private static final String ERROR_NO_CONNECTIONS = "ContainerNoConnections.msg"; //$NON-NLS-1$
	private static final String ERROR_NO_CONNECTION_WITH_URI = "ContainerNoConnectionWithURI.msg"; //$NON-NLS-1$


	private static RunConsole console;

	private static Map<IProject, ID> fidMap = new HashMap<>();

	/**
	 * This is needed for historic reasons to support getCopiedVolumes(String,
	 * String)
	 *
	 * AbstractMap.SimpleEntry is a Pair as Java does not have a native Pair
	 *
	 */
	private Map<AbstractMap.SimpleEntry<String, String>, IPath> dirMapping = new HashMap<>();

	private class CopyVolumesJob extends Job {

		private static final String COPY_VOLUMES_JOB_TITLE = "ContainerLaunch.copyVolumesJob.title"; //$NON-NLS-1$
		private static final String COPY_VOLUMES_DESC = "ContainerLaunch.copyVolumesJob.desc"; //$NON-NLS-1$
		private static final String COPY_VOLUMES_TASK = "ContainerLaunch.copyVolumesJob.task"; //$NON-NLS-1$
		private static final String ERROR_COPYING_VOLUME = "ContainerLaunch.copyVolumesJob.error"; //$NON-NLS-1$

		private final Map<String, String> volumes;
		private final IDockerConnection connection;
		private final String containerId;

		public CopyVolumesJob(Map<String, String> volumes,
				IDockerConnection connection,
				String containerId) {
			super(Messages.getString(COPY_VOLUMES_JOB_TITLE));
			this.volumes = volumes;
			this.connection = connection;
			this.containerId = containerId;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			monitor.beginTask(
					Messages.getFormattedString(COPY_VOLUMES_DESC, containerId),
					volumes.size());
			IStatus status = Status.OK_STATUS;
			// for each remote volume, copy from host to Container volume
			for (String hostDirectory : volumes.keySet()) {
				if (monitor.isCanceled()) {
					monitor.done();
					return Status.CANCEL_STATUS;
				}
				String containerDirectory = volumes.get(hostDirectory);
				if (!containerDirectory.endsWith("/")) { //$NON-NLS-1$
					containerDirectory = containerDirectory + "/"; //$NON-NLS-1$
				}
				if (!hostDirectory.endsWith("/")) { //$NON-NLS-1$
					hostDirectory = hostDirectory + "/"; //$NON-NLS-1$
				}
				monitor.setTaskName(Messages
						.getFormattedString(COPY_VOLUMES_TASK, hostDirectory));
				try {
					((DockerConnection) connection).copyToContainer(
							hostDirectory, containerId, containerDirectory);
					monitor.worked(1);
				} catch (DockerException | InterruptedException
						| IOException e) {
					monitor.done();
					final String dir = hostDirectory;
					Display.getDefault().syncExec(() -> MessageDialog.openError(
							PlatformUI.getWorkbench().getActiveWorkbenchWindow()
									.getShell(),
							Messages.getFormattedString(ERROR_COPYING_VOLUME,
									new String[] { dir, containerId }),
							e.getMessage()));
					status = Status.error(e.getMessage());
				} finally {
					monitor.done();
				}
			}
			return status;
		}

	}

	public ContainerLauncher() {
	}


	/**
	 * Perform a launch of a command in a container and output stdout/stderr to
	 * console.
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
		launch(id, listener, connectionUri, image, command, commandDir,
				workingDir, additionalDirs, origEnv, envMap, ports, keep,
				stdinSupport, false);
	}

	/**
	 * Perform a launch of a command in a container and output stdout/stderr to
	 * console.
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
	 * @param privilegedMode
	 *            - true if privileged mode is required, false otherwise
	 * @since 2.1
	 */
	public void launch(String id, IContainerLaunchListener listener,
			final String connectionUri, String image, String command,
			String commandDir, String workingDir, List<String> additionalDirs,
			Map<String, String> origEnv, Map<String, String> envMap,
			List<String> ports, boolean keep, boolean stdinSupport,
			boolean privilegedMode) {
		launch(id, listener, connectionUri, image, command, commandDir,
				workingDir, additionalDirs, origEnv, envMap, ports, keep,
				stdinSupport, privilegedMode, null);
	}

	/**
	 * Perform a launch of a command in a container and output stdout/stderr to
	 * console.
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
	 * @param privilegedMode
	 *            - true if privileged mode is required, false otherwise
	 * @param labels
	 *            - Map of labels for the container
	 * @since 2.2
	 */
	public void launch(String id, IContainerLaunchListener listener,
			final String connectionUri, String image, String command,
			String commandDir, String workingDir, List<String> additionalDirs,
			Map<String, String> origEnv, Map<String, String> envMap,
			List<String> ports, boolean keep, boolean stdinSupport,
			boolean privilegedMode, Map<String, String> labels) {
		launch(id, listener, connectionUri, image, command, commandDir,
				workingDir, additionalDirs, origEnv, envMap, ports, keep,
				stdinSupport, privilegedMode, labels, null);

	}

	// The following class allows us to use internal IConsoleListeners in
	// docker.core
	// but still use the public IRunConsoleListeners API here without requiring
	// a minor release.
	private class RunConsoleListenerBridge implements IConsoleListener {

		private IRunConsoleListener listener;

		public RunConsoleListenerBridge(IRunConsoleListener listener) {
			this.listener = listener;
		}

		@Override
		public void newOutput(String output) {
			listener.newOutput(output);
		}

	}

	/**
	 * Perform a launch of a command in a container and output stdout/stderr to
	 * console.
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
	 * @param privilegedMode
	 *            - true if privileged mode is required, false otherwise
	 * @param labels
	 *            - Map of labels for the container
	 * @param seccomp
	 *            - seccomp profile
	 * @since 3.0
	 */
	public void launch(String id, IContainerLaunchListener listener,
			final String connectionUri, String image, String command,
			@SuppressWarnings("unused") String commandDir, String workingDir,
			List<String> additionalDirs,
			Map<String, String> origEnv, Map<String, String> envMap,
			List<String> ports, boolean keep, boolean stdinSupport,
			boolean privilegedMode, Map<String, String> labels,
			String seccomp) {

		final List<String> cmdList = getCmdList(command);

		launch(id, listener, connectionUri, image, cmdList, workingDir,
				additionalDirs, origEnv, envMap, ports, keep, stdinSupport,
				privilegedMode, labels, seccomp);
	}

	/**
	 * Perform a launch of a command in a container and output stdout/stderr to
	 * console.
	 *
	 * @param id
	 *            - id of caller to use to distinguish console owner
	 * @param listener
	 *            - optional listener of the run console
	 * @param connectionUri
	 *            - the specified connection to use
	 * @param image
	 *            - the image to use
	 * @param cmdList
	 *            - command to run as list of String
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
	 * @param privilegedMode
	 *            - true if privileged mode is required, false otherwise
	 * @param labels
	 *            - Map of labels for the container
	 * @param seccomp
	 *            - seccomp profile
	 * @since 4.0
	 */
	public void launch(String id, IContainerLaunchListener listener,
			final String connectionUri, String image, List<String> cmdList,
			String workingDir, List<String> additionalDirs,
			Map<String, String> origEnv, Map<String, String> envMap,
			List<String> ports, boolean keep, boolean stdinSupport,
			boolean privilegedMode, Map<String, String> labels,
			String seccomp) {

		final String LAUNCH_TITLE = "ContainerLaunch.title"; //$NON-NLS-1$
		final String LAUNCH_EXITED_TITLE = "ContainerLaunchExited.title"; //$NON-NLS-1$

		final List<String> env = new ArrayList<>();
		env.addAll(toList(origEnv));
		env.addAll(toList(envMap));

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

		// Note we only pass volumes to the config if we have a
		// remote daemon. Local mounted volumes are passed
		// via the HostConfig binds setting

		DockerContainerConfig.Builder builder = new DockerContainerConfig.Builder()
				.env(env)
				.openStdin(stdinSupport)
				.cmd(cmdList)
				.image(image)
				.workingDir(workingDir);

		// Ugly hack...we want CDT gdbserver to run in the terminal so we look
		// for its
		// ContainerListener class and set tty=true in that case...this avoids a
		// minor release and we can later add a new launch method with the tty
		// option
		if (listener != null && listener.getClass().getName().equals(
				"org.eclipse.cdt.internal.docker.launcher.ContainerLaunchConfigurationDelegate$StartGdbServerJob")) {
			builder = builder.tty(true);
		}

		// add any exposed ports as needed
		if (!exposedPorts.isEmpty())
			builder = builder.exposedPorts(exposedPorts);

		// add any labels if specified
		if (labels != null)
			builder = builder.labels(labels);

		if (!DockerConnectionManager.getInstance().hasConnections()) {
			Display.getDefault()
					.syncExec(() -> MessageDialog.openError(
							PlatformUI.getWorkbench().getActiveWorkbenchWindow()
									.getShell(),
							DVMessages.getString(ERROR_LAUNCHING_CONTAINER),
							DVMessages.getString(ERROR_NO_CONNECTIONS)));
			return;
		}

		// Try and use the specified connection that was used before,
		// otherwise, open an error
		final IDockerConnection connection = DockerConnectionManager
				.getInstance().getConnectionByUri(connectionUri);
		if (connection == null) {
			Display.getDefault()
					.syncExec(() -> MessageDialog.openError(
							PlatformUI.getWorkbench().getActiveWorkbenchWindow()
									.getShell(),
							DVMessages.getString(ERROR_LAUNCHING_CONTAINER),
							DVMessages.getFormattedString(
									ERROR_NO_CONNECTION_WITH_URI,
									connectionUri)));
			return;
		}

		// if connection is not open, force it to be by fetching images
		if (!connection.isOpen()) {
			connection.getImages();
		}

		IDockerImageInfo imageInfo = connection.getImageInfo(image);
		if (imageInfo == null) {
			final String name = image;
			Display.getDefault()
					.syncExec(() -> MessageDialog.openError(
							PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
							DVMessages.getString(ERROR_LAUNCHING_CONTAINER),
							Messages.getFormattedString("ContainerLaunch.imageNotFound.error", name)));
			return;
		}

		IDockerContainerConfig imageConfig = imageInfo.config();
		if (imageConfig != null && imageConfig.entrypoint() != null) {
			builder = builder.entryPoint(imageConfig.entrypoint());
		}

		DockerHostConfig.Builder hostBuilder = new DockerHostConfig.Builder()
				.privileged(privilegedMode);

		// specify seccomp profile if caller has provided one - needed to use
		// ptrace with gdbserver
		if (seccomp != null) {
			hostBuilder.securityOpt(seccomp);
		}


		final Map<String, String> remoteVolumes = new HashMap<>();
		if (!((DockerConnection) connection).isLocal()) {
			@SuppressWarnings("rawtypes")
			final Map<String,Map> volumes = new HashMap<>();
			// if using remote daemon, we have to
			// handle volume mounting differently.
			// Instead we mount empty volumes and copy
			// the host data over before starting.
			if (additionalDirs != null) {
				for (String dir : additionalDirs) {
					remoteVolumes.put(dir, dir);
					volumes.put(dir, new HashMap<>());
				}
			}
			if (workingDir != null) {
				remoteVolumes.put(workingDir, workingDir); // $NON-NLS-1$
				volumes.put(workingDir, new HashMap<>());
			}
			builder = builder.volumes(volumes);
		} else {
			// Running daemon on local host.
			// Add mounts for any directories we need to run the executable.
			// When we add mount points, we need entries of the form:
			// hostname:mountname:Z.
			// In our case, we want all directories mounted as-is so the
			// executable will run as the user expects.
			final List<String> volumes = new ArrayList<>();
			if (additionalDirs != null) {
				for (String dir : additionalDirs) {
					volumes.add(dir + ":" + dir + ":Z"); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			if (workingDir != null) {
				volumes.add(workingDir + ":" + workingDir + ":Z"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			hostBuilder = hostBuilder.binds(volumes);
		}

		final DockerContainerConfig config = builder.build();

		// add any port bindings if specified
		if (portBindingsMap.size() > 0)
			hostBuilder = hostBuilder.portBindings(portBindingsMap);

		final IDockerHostConfig hostConfig = hostBuilder.build();

		if (image.equals(imageInfo.id())) {
			IDockerImage dockerImage = ((DockerConnection) connection).getImage(image);
			image = dockerImage.repoTags().get(0);
		}

		final String imageName = image;
		final boolean keepContainer = keep;
		final String consoleId = id;
		final IContainerLaunchListener containerListener = listener;

		Thread t = new Thread(() -> {
			// create the container
			String containerId = null;
			try {
				containerId = ((DockerConnection) connection)
						.createContainer(config, hostConfig, null);
				if (!((DockerConnection) connection).isLocal()) {
					// if daemon is remote, we need to copy
					// data over from the host.
					if (!remoteVolumes.isEmpty()) {
						CopyVolumesJob job = new CopyVolumesJob(remoteVolumes,
								connection, containerId);
						job.schedule();
						job.join();
						if (job.getResult() != Status.OK_STATUS)
							return;
					}
				}
				if (config.tty()) {
					// We need tty support to handle issue with Docker daemon
					// not always outputting in time (e.g. we might get an
					// output line after the process has exited which can be
					// too late to show or it might get displayed in a wrong
					// order in relation to other output. We also want the
					// output to ultimately show up in the Console View.
					OutputStream stream = null;
					RunConsole oldConsole = getConsole();
					final RunConsole rc = RunConsole.findConsole(containerId,
							consoleId);
					setConsole(rc);
					rc.clearConsole();
					if (oldConsole != null)
						RunConsole.removeConsole(oldConsole);
					Display.getDefault().syncExec(() -> rc.setTitle(Messages
							.getFormattedString(LAUNCH_TITLE, new String[] {
									cmdList.get(0), imageName })));
					if (rc != null) {
						stream = rc.getOutputStream();
					}

					// We want terminal support, but we want to output to the
					// RunConsole.
					// To do this, we create a DockerConsoleOutputStream which
					// we
					// hook into the TM Terminal via stdout and stderr output
					// listeners.
					// These listeners will output to the
					// DockerConsoleOutputStream which
					// will in turn output to the RunConsole. See
					// DockerConnection.openTerminal().
					DockerConsoleOutputStream out = new DockerConsoleOutputStream(
							stream);
					RunConsole.attachToTerminal(connection, containerId, out);
					if (containerListener != null) {
						out.addConsoleListener(new RunConsoleListenerBridge(
								containerListener));
					}
					((DockerConnection) connection).startContainer(containerId,
							null, null);
					IDockerContainerInfo info = ((DockerConnection) connection)
							.getContainerInfo(containerId);
					if (containerListener != null) {
						containerListener.containerInfo(info);
					}
					// Wait for the container to finish
					final IDockerContainerExit status = ((DockerConnection) connection)
							.waitForContainer(containerId);
					Display.getDefault().syncExec(() -> {
						rc.setTitle(
								Messages.getFormattedString(LAUNCH_EXITED_TITLE,
										new String[] {
												status.statusCode().toString(),
												cmdList.get(0), imageName }));
						rc.showConsole();
						// We used a TM Terminal to receive the output of the
						// session and
						// then sent the output to the RunConsole. Remove the
						// terminal
						// tab that got created now that we are finished and all
						// data is shown
						// in Console View.
						IWorkbenchPage page = PlatformUI.getWorkbench()
								.getActiveWorkbenchWindow().getActivePage();
						IViewPart terminalView = page.findView(
								"org.eclipse.terminal.view.ui.TerminalsView");
						CTabFolder ctabfolder = terminalView
								.getAdapter(CTabFolder.class);
						if (ctabfolder != null) {
							CTabItem[] items = ctabfolder.getItems();
							for (CTabItem item : items) {
								if (item.getText().endsWith(info.name())) {
									item.dispose();
									break;
								}
							}
						 }
					});
					// Let any container listener know that the container is
					// finished
					if (containerListener != null)
						containerListener.done();

					if (!keepContainer) {
						((DockerConnection) connection)
								.removeContainer(containerId);
					}
				} else {
					OutputStream stream = null;
					RunConsole oldConsole = getConsole();
					final RunConsole rc = RunConsole.findConsole(containerId,
							consoleId);
					setConsole(rc);
					rc.clearConsole();
					if (oldConsole != null)
						RunConsole.removeConsole(oldConsole);
					Display.getDefault().syncExec(() -> rc.setTitle(Messages
							.getFormattedString(LAUNCH_TITLE, new String[] {
									cmdList.get(0), imageName })));
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
					Display.getDefault().syncExec(() -> {
						rc.setTitle(
								Messages.getFormattedString(LAUNCH_EXITED_TITLE,
										new String[] {
												status.statusCode().toString(),
												cmdList.get(0), imageName }));
						rc.showConsole();
					});

					// Let any container listener know that the container is
					// finished
					if (containerListener != null)
						containerListener.done();

					if (!keepContainer) {
						// Drain the logging thread before we remove the
						// container (we need to use the logging id)
						Thread.sleep(1000);
						((DockerConnection) connection)
								.stopLoggingThread(loggingId);
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
				}

			} catch (final DockerException e2) {
				// error in creation, try and remove Container if possible
				if (!keepContainer && containerId != null) {
					try {
						((DockerConnection) connection)
								.removeContainer(containerId);
					} catch (DockerException | InterruptedException e1) {
						// ignore exception
					}
				}
				Display.getDefault().syncExec(() -> MessageDialog.openError(
						PlatformUI.getWorkbench().getActiveWorkbenchWindow()
								.getShell(),
						DVMessages.getFormattedString(ERROR_CREATING_CONTAINER,
								imageName),
						e2.getMessage()));
			} catch (InterruptedException e3) {
				// for now
				// do nothing
			}
			((DockerConnection) connection).getContainers(true);
		});
		t.start();
	}

	private class ID {
		private Integer uid;
		private Integer gid;

		public ID(Integer uid, Integer gid) {
			this.uid = uid;
			this.gid = gid;
		}

		public Integer getuid() {
			return uid;
		}

		public Integer getgid() {
			return gid;
		}
	}

	/**
	 * Fetch directories from Container and place them in a specified location.
	 *
	 * @param connectionUri
	 *            - uri of connection to use
	 * @param imageName
	 *            - name of image to use
	 * @param containerDirs
	 *            - list of directories to copy
	 * @param hostDir
	 *            - host directory to copy directories to
	 * @return 0 if successful, -1 if failure occurred
	 *
	 * @since 3.0
	 */
	public int fetchContainerDirs(String connectionUri, String imageName,
			List<String> containerDirs, IPath hostDir) {

		return fetchContainerDirs(connectionUri, imageName,
				containerDirs, null, hostDir);
	}

	/**
	 * Fetch directories from Container and place them in a specified location.
	 *
	 * @param connectionUri
	 *            - uri of connection to use
	 * @param imageName
	 *            - name of image to use
	 * @param containerDirs
	 *            - list of directories to copy
	 * @param excludedDirs
	 *            - list of directories not to copy
	 * @param hostDir
	 *            - host directory to copy directories to
	 * @return 0 if successful, -1 if failure occurred
	 *
	 * @since 4.0
	 */
	public int fetchContainerDirs(String connectionUri, String imageName,
			List<String> containerDirs, List<String> excludedDirs, IPath hostDir) {
		Job job = fetchContainerDirsJobInt(connectionUri, imageName, containerDirs, excludedDirs, hostDir);
		job.schedule();
		return 0;
	}

	/**
	 * Fetch directories from Container and place them in a specified location.
	 * This method will wait for copy job to complete before returning.
	 *
	 * @param connectionUri
	 *            - uri of connection to use
	 * @param imageName
	 *            - name of image to use
	 * @param containerDirs
	 *            - list of directories to copy
	 * @param hostDir
	 *            - host directory to copy directories to
	 * @return 0 if successful, -1 if failure occurred
	 *
	 * @since 4.0
	 */
	public int fetchContainerDirsSync(String connectionUri, String imageName,
			List<String> containerDirs, IPath hostDir) {
		return fetchContainerDirsSync(connectionUri, imageName, containerDirs, null, hostDir);
	}

	/**
	 * Fetch directories from Container and place them in a specified location.
	 * This method will wait for copy job to complete before returning.
	 *
	 * @param connectionUri
	 *            - uri of connection to use
	 * @param imageName
	 *            - name of image to use
	 * @param containerDirs
	 *            - list of directories to copy
	 * @param hostDir
	 *            - host directory to copy directories to
	 * @return 0 if successful, -1 if failure occurred
	 *
	 * @since 4.0
	 */
	public int fetchContainerDirsSync(String connectionUri, String imageName,
			List<String> containerDirs, List<String> excludedDirs,
			IPath hostDir) {

		Job job =  fetchContainerDirsJobInt(connectionUri, imageName,containerDirs,
				excludedDirs, hostDir);
		job.schedule();
		try {
			job.join();
		} catch (InterruptedException e) {
			return -1;
		}
		return 0;
	}

	private Job fetchContainerDirsJobInt(String connectionUri, String imageName, List<String> containerDirs,
			List<String> excludedDirs, IPath hostDir) {
		// Try and use the specified connection that was used before,
		// otherwise, open an error
		final IDockerConnection connection = DockerConnectionManager.getInstance().getConnectionByUri(connectionUri);
		if (connection == null) {
			Display.getDefault()
					.syncExec(() -> MessageDialog.openError(
							PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
							DVMessages.getString(ERROR_LAUNCHING_CONTAINER),
							DVMessages.getFormattedString(ERROR_NO_CONNECTION_WITH_URI, connectionUri)));
			return null;
		}

		HashSet<Path> containerDirsSet = new HashSet<>();

		for (String dir : containerDirs) {
			if (excludedDirs != null && excludedDirs.stream().anyMatch(dir::startsWith)) {
				continue;
			}
			containerDirsSet.add(new Path(dir));

		}

		// This is needed for backwards compatibility
		dirMapping.put(new AbstractMap.SimpleEntry<>(connectionUri, imageName), hostDir);

		return new CopyFromDockerJob(connection, CopyType.ImageMirror, imageName, containerDirsSet, (Path) hostDir);
	}

	/**
	 * Create a Process to run an arbitrary command in a Container with uid of
	 * caller so any files created are accessible to user.
	 *
	 * @param connectionName
	 *            - uri of connection to use
	 * @param imageName
	 *            - name of image to use
	 * @param project
	 *            - Eclipse project
	 * @param errMsgHolder
	 *            - holder for any error messages
	 * @param command
	 *            - command to run
	 * @param commandDir
	 *            - directory path to command (unused)
	 * @param workingDir
	 *            - where to run command
	 * @param additionalDirs
	 *            - additional directories to mount
	 * @param origEnv
	 *            - original environment if we are appending to existing
	 * @param envMap
	 *            - new environment
	 * @param supportStdin
	 *            - support using stdin
	 * @param privilegedMode
	 *            - run in privileged mode
	 * @param labels
	 *            - labels to apply to Container
	 * @param keepContainer
	 *            - boolean whether to keep Container when done
	 * @return Process that can be used to check for completion and for routing
	 *         stdout/stderr
	 *
	 * @since 3.0
	 */
	public Process runCommand(String connectionName, String imageName, IProject project,
			IErrorMessageHolder errMsgHolder, String command,
			@SuppressWarnings("unused") String commandDir,
			String workingDir,
			List<String> additionalDirs, Map<String, String> origEnv,
			Properties envMap, boolean supportStdin,
			boolean privilegedMode, HashMap<String, String> labels,
			boolean keepContainer) {

		final List<String> cmdList = getCmdList(command);
		return runCommand(connectionName, imageName, project, errMsgHolder,
				cmdList, workingDir, additionalDirs, origEnv, envMap,
				supportStdin, privilegedMode, labels, keepContainer);
	}

	/**
	 * Create a Process to run an arbitrary command in a Container with uid of
	 * caller so any files created are accessible to user.
	 *
	 * additionalDirs must be either
	 *  * local path that will be mapped to the same path within the container
	 *  * <ContainerPath>:CONTAINER:<Sourcepath>:<Bool:selected>
	 *  * <ContainerPath>:HOST_PATH:<Sourcepath>:<Bool:ro>:<Bool:selected>
	 *  * <ContainerPath>:None:<Bool:selected>
	 * See DataVolumeModel for details.
	 * @param connectionName
	 *            - uri of connection to use
	 * @param imageName
	 *            - name of image to use
	 * @param project
	 *            - Eclipse project
	 * @param errMsgHolder
	 *            - holder for any error messages
	 * @param cmdList
	 *            - command to run as list of String
	 * @param workingDir
	 *            - where to run command
	 * @param additionalDirs
	 *            - See description
	 * @param origEnv
	 *            - original environment if we are appending to existing
	 * @param envMap
	 *            - new environment
	 * @param supportStdin
	 *            - support using stdin
	 * @param privilegedMode
	 *            - run in privileged mode
	 * @param labels
	 *            - labels to apply to Container
	 * @param keepContainer
	 *            - boolean whether to keep Container when done
	 * @return Process that can be used to check for completion and for routing
	 *         stdout/stderr
	 *
	 * @since 4.0
	 */
	public Process runCommand(String connectionName, String imageName,
			IProject project, IErrorMessageHolder errMsgHolder,
			List<String> cmdList,
			String workingDir, List<String> additionalDirs,
			Map<String, String> origEnv, Properties envMap,
			boolean supportStdin, boolean privilegedMode,
			HashMap<String, String> labels, boolean keepContainer) {
		return runCommand(workingDir, workingDir, project, errMsgHolder, additionalDirs, workingDir, additionalDirs,
				origEnv, envMap, keepContainer, keepContainer, labels, keepContainer, null);
	}

	/**
	 * Create a Process to run an arbitrary command in a Container with uid of
	 * caller so any files created are accessible to user.
	 *
	 * additionalDirs must be either * local path that will be mapped to the same
	 * path within the container *
	 * <ContainerPath>:CONTAINER:<Sourcepath>:<Bool:selected> *
	 * <ContainerPath>:HOST_PATH:<Sourcepath>:<Bool:ro>:<Bool:selected> *
	 * <ContainerPath>:None:<Bool:selected> See DataVolumeModel for details.
	 *
	 * @param connectionName - uri of connection to use
	 * @param imageName      - name of image to use
	 * @param project        - Eclipse project
	 * @param errMsgHolder   - holder for any error messages
	 * @param cmdList        - command to run as list of String
	 * @param workingDir     - where to run command
	 * @param additionalDirs - See description
	 * @param origEnv        - original environment if we are appending to existing
	 * @param envMap         - new environment
	 * @param supportStdin   - support using stdin
	 * @param privilegedMode - run in privileged mode
	 * @param labels         - labels to apply to Container
	 * @param keepContainer  - boolean whether to keep Container when done
	 * @param seccomp        - list of strings containing seccomp settings or null
	 * @return Process that can be used to check for completion and for routing
	 *         stdout/stderr
	 *
	 * @since 5.12
	 */
	public Process runCommand(String connectionName, String imageName, IProject project,
			IErrorMessageHolder errMsgHolder, List<String> cmdList, String workingDir, List<String> additionalDirs,
			Map<String, String> origEnv, Properties envMap, boolean supportStdin, boolean privilegedMode,
			HashMap<String, String> labels, boolean keepContainer, List<String> seccomp) {
		Integer uid = null;
		Integer gid = null;
		// For Unix, make sure that the user id is passed with the run
		// so any output files are accessible by this end-user
		String os = System.getProperty("os.name"); //$NON-NLS-1$
		if (os.indexOf("nux") > 0) { //$NON-NLS-1$
			// first try and see if we have already run a command on this
			// project
			ID ugid = fidMap.get(project);
			if (ugid == null) {
				try {
					uid = (Integer) Files.getAttribute(
							project.getLocation().toFile().toPath(),
							"unix:uid"); //$NON-NLS-1$
					gid = (Integer) Files.getAttribute(
							project.getLocation().toFile().toPath(),
							"unix:gid"); //$NON-NLS-1$
					ugid = new ID(uid, gid);
					// store the uid for possible later usage
					fidMap.put(project, ugid);
				} catch (IOException e) {
					// do nothing...leave as null
				} // $NON-NLS-1$
			} else {
				uid = ugid.getuid();
				gid = ugid.getgid();
			}
		}

		final List<String> env = new ArrayList<>();
		env.addAll(toList(origEnv));
		env.addAll(toList(envMap));


		final Map<String, List<IDockerPortBinding>> portBindingsMap = new HashMap<>();


		IDockerConnection[] connections = DockerConnectionManager
				.getInstance().getConnections();
		if (connections == null || connections.length == 0) {
			errMsgHolder.setErrorMessage(
					Messages.getString("ContainerLaunch.noConnections.error")); //$NON-NLS-1$
			return null;
		}

		IDockerConnection connection = null;
		for (IDockerConnection c : connections) {
			if (c.getUri().equals(connectionName)) {
				connection = c;
				break;
			}
		}

		if (connection == null) {
			errMsgHolder.setErrorMessage(Messages.getFormattedString(
					"ContainerLaunch.connectionNotFound.error", //$NON-NLS-1$
					connectionName));
			return null;
		}

		List<IDockerImage> images = connection.getImages();
		if (images.isEmpty()) {
			errMsgHolder.setErrorMessage(
					Messages.getString("ContainerLaunch.noImages.error")); //$NON-NLS-1$
			return null;
		}

		IDockerImageInfo info = connection.getImageInfo(imageName);
		if (info == null) {
			errMsgHolder.setErrorMessage(Messages.getFormattedString(
					"ContainerLaunch.imageNotFound.error", imageName)); //$NON-NLS-1$
			return null;
		}

		DockerContainerConfig.Builder builder = new DockerContainerConfig.Builder()
				.openStdin(supportStdin).cmd(cmdList).image(imageName)
				.workingDir(workingDir);

		// preserve any entry point specified in the image
		if (info.config() != null) {
			List<String> entrypoint = info.config().entrypoint();
			if (entrypoint != null && !entrypoint.isEmpty()) {
				builder = builder.entryPoint(entrypoint);
			}
		}
		// switch to user id and group id for Linux so output is accessible
		if (uid != null) {
			String id = uid.toString();
			if (gid != null)
				id += ":" + gid.toString(); //$NON-NLS-1$
			builder = builder.user(id);
		}

		// TODO: add group id here when supported by DockerHostConfig.Builder

		// add any labels if specified
		if (labels != null)
			builder = builder.labels(labels);

		DockerHostConfig.Builder hostBuilder = new DockerHostConfig.Builder()
				.privileged(privilegedMode);

		if (seccomp != null) {
			hostBuilder = hostBuilder.securityOpt(seccomp);
		}

		// Note we only pass volumes to the config if we have a
		// remote daemon. Local mounted volumes are passed
		// via the HostConfig binds setting
		@SuppressWarnings("rawtypes")
		final Map<String, Map> remoteVolumes = new HashMap<>();
		final Map<String, String> remoteDataVolumes = new HashMap<>();
		final Set<String> readOnlyVolumes = new TreeSet<>();
		if (!((DockerConnection) connection).isLocal()) {
			// if using remote daemon, we have to
			// handle volume mounting differently.
			// Instead we mount empty volumes and copy
			// the host data over before starting.
			if (additionalDirs != null) {
				for (String dir : additionalDirs) {
					IPath p = new Path(dir).removeTrailingSeparator();
					remoteVolumes.put(p.toPortableString(), new HashMap<>());
					remoteDataVolumes.put(p.toPortableString(),
							p.toPortableString());
					if (dir.contains(":")) { //$NON-NLS-1$
						DataVolumeModel dvm = DataVolumeModel.parseString(dir);
						switch (dvm.getMountType()) {
						case HOST_FILE_SYSTEM:
							dir = dvm.getHostPathMount();
							remoteDataVolumes.put(dir, dvm.getContainerMount());
							// keep track of read-only volumes so we don't copy
							// these
							// back after command completion
							if (dvm.isReadOnly()) {
								readOnlyVolumes.add(dir);
							}
							break;
						default:
							continue;
						}
					}
				}
			}
			if (workingDir != null) {
				IPath p = new Path(workingDir).removeTrailingSeparator();
				remoteVolumes.put(p.toPortableString(), new HashMap<>());
				remoteDataVolumes.put(p.toPortableString(),
						p.toPortableString());
			}
			// Check volumes known by the connection to see if user has specified a
			// volume_name:container_dir as an additional dir. In such a case, we
			// don't need to copy data over and can simply bind to the volume.
			try {
				final List<String> volumes = new ArrayList<>();
				List<IDockerVolume> volumeList = ((DockerConnection) connection).getVolumes();
				for (IDockerVolume volume : volumeList) {
					String name = volume.name();
					String containerDir = remoteDataVolumes.get(name);
					if (containerDir != null) {
						if (readOnlyVolumes.contains(name)) {
							volumes.add(name + ":" + containerDir + ":Z,ro"); //$NON-NLS-1$ //$NON-NLS-2$
							readOnlyVolumes.remove(name);
						} else {
							volumes.add(name + ":" + containerDir + ":Z"); //$NON-NLS-1$ //$NON-NLS-2$
						}
						remoteVolumes.remove(name);
						remoteDataVolumes.remove(name);
						IPath p = new Path(containerDir);
						// if the working dir is a subdirectory of the volume mount, we don't need to
						// copy it
						// forward or back
						if (workingDir != null) {
							IPath working = new Path(workingDir);
							if (p.isPrefixOf(working)) {
								String s = working.removeTrailingSeparator().toPortableString();
								remoteVolumes.remove(s);
								remoteDataVolumes.remove(s);
							}
						}
					}
				}
				// bind any volumes we found above
				if (!volumes.isEmpty()) {
					hostBuilder = hostBuilder.binds(volumes);
				}
			} catch (DockerException e) {
				Activator.log(e);
			}
			if (!remoteVolumes.isEmpty()) {
				builder = builder.volumes(remoteVolumes);
			}
		} else {
			// Running daemon on local host.
			// Add mounts for any directories we need to run the executable.
			// When we add mount points, we need entries of the form:
			// hostname:mountname:Z.
			// In our case, we want all directories mounted as-is so the
			// executable will run as the user expects.
			final Set<String> volumes = new TreeSet<>();
			final List<String> volumesFrom = new ArrayList<>();
			if (additionalDirs != null) {
				for (String dir : additionalDirs) {
					IPath p = new Path(dir).removeTrailingSeparator();
					if (dir.contains(":")) { //$NON-NLS-1$
						DataVolumeModel dvm = DataVolumeModel.parseString(dir);
						switch (dvm.getMountType()) {
						case HOST_FILE_SYSTEM:
							String bind = dvm.getHostPathMount()
									+ ':' + dvm.getContainerPath() + ":Z"; //$NON-NLS-1$ //$NON-NLS-2$
							if (dvm.isReadOnly()) {
								bind += ",ro"; //$NON-NLS-1$
							}
							volumes.add(bind);
							break;
						case CONTAINER:
							volumesFrom.add(dvm.getContainerMount());
							break;
						default:
							break;

						}
					} else {
						volumes.add(p.toPortableString() + ":" //$NON-NLS-1$
								+ p.toPortableString() + ":Z"); //$NON-NLS-1$
					}
				}
			}
			List<String> volumeList = new ArrayList<>(volumes);
			hostBuilder = hostBuilder.binds(volumeList);
			if (!volumesFrom.isEmpty()) {
				hostBuilder = hostBuilder.volumesFrom(volumesFrom);
			}
		}

		final DockerContainerConfig config = builder.build();

		// add any port bindings if specified
		if (portBindingsMap.size() > 0)
			hostBuilder = hostBuilder.portBindings(portBindingsMap);

		final IDockerHostConfig hostConfig = hostBuilder.build();

		// create the container
		String containerId = null;
		try {
			containerId = ((DockerConnection) connection)
					.createContainer(config, hostConfig, null);
			// Add delay after creating container to fix bug 546505
			Thread.sleep(100);
		} catch (DockerException | InterruptedException e) {
			errMsgHolder.setErrorMessage(e.getMessage());
			return null;
		}

		final String id = containerId;
		final IDockerConnection conn = connection;
		if (!((DockerConnection) conn).isLocal()) {
			// if daemon is remote, we need to copy
			// data over from the host.
			if (!remoteVolumes.isEmpty()) {
				CopyVolumesJob job = new CopyVolumesJob(remoteDataVolumes, conn,
						id);
				job.schedule();
				try {
					job.join();
				} catch (InterruptedException e) {
					// ignore
				}
			}
		}

		// remove all read-only remote volumes from our list of remote
		// volumes so they won't be copied back on command completion
		for (String readonly : readOnlyVolumes) {
			remoteDataVolumes.remove(readonly);
		}
		return new ContainerCommandProcess(connection, imageName, containerId,
				null,
				remoteDataVolumes,
				keepContainer);
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
		if (!DockerConnectionManager.getInstance().hasConnections()) {
			return;
		}

		// Try and find the specified connection
		final IDockerConnection connection = DockerConnectionManager
				.getInstance().getConnectionByUri(connectionUri);
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
		StringBuilder buffer = new StringBuilder();
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
	private List<String> toList(
			@SuppressWarnings("rawtypes") final Map variables) {
		final List<String> result = new ArrayList<>();
		if (variables != null) {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			Set<Map.Entry> entries = variables.entrySet();
			for (@SuppressWarnings("rawtypes")
			Map.Entry entry : entries) {
				final String key = (String) entry.getKey();
				final String value = (String) entry.getValue();

				final String envEntry = key + "=" + value; //$NON-NLS-1$
				result.add(envEntry);
			}
		}
		return result;

	}

	/**
	 * Get set of volumes that have been copied from Container to Host as part of
	 * {@link fetchContainerDirs} method.
	 *
	 * @param connectionName - uri of connection used
	 * @param imageName      - name of image used
	 * @return set of paths copied from Container to Host
	 *
	 * @since 3.0
	 * @deprecated Use the alternate {@link getCopiedVolumes(IPath)} using the
	 *             folder where the data was copied to. This is where the
	 *             information is kept.
	 */
	@Deprecated
	public Set<String> getCopiedVolumes(String connectionName, String imageName) {
		IPath tpath = dirMapping.get(new AbstractMap.SimpleEntry<>(connectionName, imageName));
		if(tpath == null) return new HashSet<>();

		Set<IPath> rv = getCopiedVolumes(tpath);
		return rv.stream().map(IPath::toString).collect(Collectors.toSet());
	}

	/**
	 * Get set of volumes that have been copied from Container to Host as part of
	 * fetchContainerDirs method.
	 *
	 * @param hostdir The {@code hostdir} param used in {@link fetchContainerDirs}
	 * @return set of paths copied from Container to Host
	 *
	 * @since 5.7
	 */
	public static Set<IPath> getCopiedVolumes(IPath hostdir) {
		return CopyFromDockerJob.getCopiedPaths((Path) hostdir);
	}

}
