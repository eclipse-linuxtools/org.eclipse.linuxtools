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

package org.eclipse.linuxtools.internal.docker.ui.launch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerContainerConfig;
import org.eclipse.linuxtools.docker.core.IDockerHostConfig;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.docker.core.IDockerPortBinding;
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.linuxtools.internal.docker.core.DockerContainerConfig;
import org.eclipse.linuxtools.internal.docker.core.DockerContainerConfig.Builder;
import org.eclipse.linuxtools.internal.docker.core.DockerHostConfig;
import org.eclipse.linuxtools.internal.docker.ui.SWTImagesFactory;
import org.eclipse.linuxtools.internal.docker.ui.commands.RunImageCommandHandler;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

/**
 * Launch configuration delegate for running Docker Images
 */
public class RunDockerImageLaunchConfigurationDelegate
		implements ILaunchConfigurationDelegate {

	private static final String LaunchShortcut_Connection_Selection = "ImageBuildShortcutConnectionSelection.title"; //$NON-NLS-1$
	private static final String LaunchShortcut_Choose_Connection = "ImageBuildShortcutChooseConnection.msg"; //$NON-NLS-1$

	@Override
	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) {
		try {
			ILaunchConfiguration config = launch.getLaunchConfiguration();
			final IDockerContainerConfig containerConfig = getDockerContainerConfig(
					config);
			final IDockerHostConfig hostConfig = getDockerHostConfig(config);
			final IDockerConnection connection = getDockerConnection(config);
			if (connection == null)
				return;
			final IDockerImage image = getDockerImage(config, connection);
			RunImageCommandHandler.runImage(image,
					containerConfig,
					hostConfig,
					config.getAttribute(
							IRunDockerImageLaunchConfigurationConstants.CONTAINER_NAME,
							(String) null),
					config.getAttribute(
							IRunDockerImageLaunchConfigurationConstants.AUTO_REMOVE,
							false));
		} catch (CoreException e) {
			Activator.log(e);
		}
	}

	private IDockerImage getDockerImage(final ILaunchConfiguration config,
			final IDockerConnection connection) throws CoreException {
		final String imageId = config.getAttribute(
				IRunDockerImageLaunchConfigurationConstants.IMAGE_ID,
				(String) null);
		if (imageId != null) {
			return ((DockerConnection) connection).getImage(imageId);
		}
		return null;
	}

	public IDockerHostConfig getDockerHostConfig(ILaunchConfiguration config)
			throws CoreException {
		final DockerHostConfig.Builder hostConfigBuilder = new DockerHostConfig.Builder();
		if (config.getAttribute(
				IRunDockerImageLaunchConfigurationConstants.PUBLISH_ALL_PORTS,
				false)) {
			hostConfigBuilder.publishAllPorts(true);
		} else {
			final List<String> serializedBindings = config.getAttribute(
					IRunDockerImageLaunchConfigurationConstants.PUBLISHED_PORTS,
					new ArrayList<String>());
			final Map<String, List<IDockerPortBinding>> portBindings = LaunchConfigurationUtils
					.deserializePortBindings(serializedBindings);
			hostConfigBuilder.portBindings(portBindings);
		}
		
		// container links
		final List<String> links = config.getAttribute(
				IRunDockerImageLaunchConfigurationConstants.LINKS,
				new ArrayList<String>());
		hostConfigBuilder.links(links);

		// data volumes
		final List<String> volumesFromList = config.getAttribute(IRunDockerImageLaunchConfigurationConstants.VOLUMES_FROM, new ArrayList<String>());
		hostConfigBuilder.volumesFrom(volumesFromList);
		final List<String> binds = new ArrayList<>();
		final List<String> bindsList = config.getAttribute(IRunDockerImageLaunchConfigurationConstants.BINDS, new ArrayList<String>());
		for (String bindsListEntry : bindsList) {
			String[] bindsEntryParms = bindsListEntry.split(":"); //$NON-NLS-1$
			final StringBuilder bind = new StringBuilder();
			bind.append(LaunchConfigurationUtils
					.convertToUnixPath(bindsEntryParms[0])).append(':')
					.append(bindsEntryParms[1]).append(":Z");
			if (bindsEntryParms[2].equals("true")) {
				bind.append(",ro"); //$NON-NLS-1$
			}
			binds.add(bind.toString());
					
		}
		hostConfigBuilder.binds(binds);
		
		// run in privileged mode
		final boolean privileged = config.getAttribute(
				IRunDockerImageLaunchConfigurationConstants.PRIVILEGED,
				false);
		hostConfigBuilder.privileged(privileged);

		final String networkMode = config.getAttribute(
				IRunDockerImageLaunchConfigurationConstants.NETWORK_MODE, ""); //$NON-NLS-1$
		if (!networkMode.isEmpty()) {
			hostConfigBuilder.networkMode(networkMode);
		}

		return hostConfigBuilder.build();
	}

	public IDockerContainerConfig getDockerContainerConfig(
			ILaunchConfiguration lconfig) throws CoreException {

		String command = lconfig.getAttribute(
				IRunDockerImageLaunchConfigurationConstants.COMMAND, ""); //$NON-NLS-1$
		String entrypoint = lconfig.getAttribute(
				IRunDockerImageLaunchConfigurationConstants.ENTRYPOINT, ""); //$NON-NLS-1$
		String imageName = lconfig.getAttribute(
				IRunDockerImageLaunchConfigurationConstants.IMAGE_NAME, ""); //$NON-NLS-1$
		boolean useTTY = lconfig.getAttribute(
				IRunDockerImageLaunchConfigurationConstants.ALLOCATE_PSEUDO_CONSOLE,
				false);
		boolean interactive = lconfig.getAttribute(
				IRunDockerImageLaunchConfigurationConstants.INTERACTIVE, false);

		final Builder config = new DockerContainerConfig.Builder().cmd(command)
				.entryPoint(entrypoint).image(imageName).tty(useTTY)
				.openStdin(interactive);
		boolean limits_enabled = lconfig.getAttribute(
				IRunDockerImageLaunchConfigurationConstants.ENABLE_LIMITS,
				false);
		if (limits_enabled) {
			long memory = Long.parseLong(lconfig.getAttribute(
					IRunDockerImageLaunchConfigurationConstants.MEMORY_LIMIT,
					"0"));
			config.memory(memory);
			long cpuShares = Long.parseLong(lconfig.getAttribute(
					IRunDockerImageLaunchConfigurationConstants.CPU_PRIORITY,
					"0"));
			config.cpuShares(cpuShares);
		}
		// environment variables
		final List<String> environmentVariables = lconfig.getAttribute(
				IRunDockerImageLaunchConfigurationConstants.ENV_VARIABLES,
				new ArrayList<String>());
		config.env(environmentVariables);
		
		// labels
		final Map<String, String> labelVariables = lconfig.getAttribute(
				IRunDockerImageLaunchConfigurationConstants.LABELS,
				(Map<String, String>) null);
		if (labelVariables != null)
			config.labels(labelVariables);

		return config.build();
	}

	private IDockerConnection getDockerConnection(ILaunchConfiguration config)
			throws CoreException {
		String configName = config.getAttribute(
				IRunDockerImageLaunchConfigurationConstants.CONNECTION_NAME,
				""); //$NON-NLS-1$
		IDockerConnection connection = DockerConnectionManager.getInstance()
				.findConnection(configName);
		if (connection == null) {
			connection = chooseConnection(
					DockerConnectionManager.getInstance().getConnections());
		}
		return connection;
	}


	private class ConnectionSelectionLabelProvider implements ILabelProvider {

		private Image CONNECTION_IMAGE = SWTImagesFactory.DESC_REPOSITORY_MIDDLE
				.createImage();

		@Override
		public void removeListener(ILabelProviderListener listener) {
		}

		@Override
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		@Override
		public void dispose() {
			CONNECTION_IMAGE.dispose();
		}

		@Override
		public void addListener(ILabelProviderListener listener) {
		}

		@Override
		public String getText(Object element) {
			return element.toString();
		}

		@Override
		public Image getImage(Object element) {
			return CONNECTION_IMAGE;
		}
	}

	/**
	 * Show a selection dialog that allows the user to choose one of the
	 * connections to use to build the Image.
	 * 
	 * @param connections
	 *            Array of connections.
	 * @return The chosen connection, or <code>null</code> if the user cancelled
	 *         the dialog.
	 */
	protected IDockerConnection chooseConnection(
			final IDockerConnection[] connections) {
		IDebugModelPresentation labelProvider = DebugUITools
				.newDebugModelPresentation();
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(
				getActiveWorkbenchShell(),
				new ConnectionSelectionLabelProvider() {

				});

		dialog.setElements(connections);
		dialog.setTitle(
				LaunchMessages.getString(LaunchShortcut_Connection_Selection));
		dialog.setMessage(
				LaunchMessages.getString(LaunchShortcut_Choose_Connection));
		dialog.setMultipleSelection(false);
		int result = dialog.open();
		labelProvider.dispose();
		if (result == IStatus.OK) {
			return (IDockerConnection) dialog.getFirstResult();
		}
		return null;
	}

	/**
	 * Get the active Workbench shell.
	 * 
	 * @return active shell as returned by the plug-in
	 */
	protected Shell getActiveWorkbenchShell() {
		return Activator.getActiveWorkbenchShell();
	}

}
