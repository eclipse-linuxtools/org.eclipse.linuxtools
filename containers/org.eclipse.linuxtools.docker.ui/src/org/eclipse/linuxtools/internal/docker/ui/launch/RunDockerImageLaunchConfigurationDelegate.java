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
import java.util.Arrays;
import java.util.HashMap;
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
import org.eclipse.linuxtools.internal.docker.core.DockerPortBinding;
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
			final Map<String, List<IDockerPortBinding>> portBindings = new HashMap<>();
			Map<String, String> ports = config.getAttribute(
					IRunDockerImageLaunchConfigurationConstants.PUBLISHED_PORTS,
					new HashMap<String, String>());
			for (Map.Entry<String, String> entry : ports.entrySet()) {
				String key = entry.getKey();
				String entryValue = entry.getValue();

				String[] portPairs = entryValue.split("\\s*,\\s*"); //$NON-NLS-1$

				for (int i = 0; i < portPairs.length; i += 2) {
					DockerPortBinding portBinding = new DockerPortBinding(
							portPairs[i], portPairs[i + 1]);
					portBindings.put(key,
							Arrays.<IDockerPortBinding> asList(portBinding));
				}
			}
			hostConfigBuilder.portBindings(portBindings);
		}
		// container links
		final List<String> links = config.getAttribute(
				IRunDockerImageLaunchConfigurationConstants.LINKS,
				new ArrayList<String>());
		hostConfigBuilder.links(links);

		// data volumes
		
		List<String> volumesFromList = config.getAttribute(IRunDockerImageLaunchConfigurationConstants.VOLUMES_FROM, new ArrayList<String>());
		hostConfigBuilder.volumesFrom(volumesFromList);
		
		final List<String> binds = new ArrayList<>();
		
		List<String> bindsList = config.getAttribute(IRunDockerImageLaunchConfigurationConstants.BINDS, new ArrayList<String>());

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
		
		return hostConfigBuilder.build();
	}

	public IDockerContainerConfig getDockerContainerConfig(
			ILaunchConfiguration lconfig) throws CoreException {

		String commandAsString = lconfig.getAttribute(
				IRunDockerImageLaunchConfigurationConstants.COMMAND, ""); //$NON-NLS-1$
		List<String> command = getCmdList(commandAsString);
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
			int memory = lconfig.getAttribute(
					IRunDockerImageLaunchConfigurationConstants.MEMORY_LIMIT,
					0);
			int cpuShares = lconfig.getAttribute(
					IRunDockerImageLaunchConfigurationConstants.CPU_PRIORITY,
					0);
			config.memory(Long.valueOf(memory) * 1048576);
			config.cpuShares(Long.valueOf(cpuShares));
		}
		// environment variables
		final List<String> environmentVariables = lconfig.getAttribute(
				IRunDockerImageLaunchConfigurationConstants.ENV_VARIABLES,
				new ArrayList<String>());
		config.env(environmentVariables);
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

	// Create a proper command list after handling quotation.
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

	private class ConnectionSelectionLabelProvider implements ILabelProvider {
		@Override
		public void removeListener(ILabelProviderListener listener) {
		}

		@Override
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		@Override
		public void dispose() {
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
			return SWTImagesFactory.DESC_REPOSITORY_MIDDLE.createImage();
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
