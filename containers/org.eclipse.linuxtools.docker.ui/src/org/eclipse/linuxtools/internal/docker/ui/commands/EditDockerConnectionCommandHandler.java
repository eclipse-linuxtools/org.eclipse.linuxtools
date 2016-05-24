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

package org.eclipse.linuxtools.internal.docker.ui.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerImageBuildOptions;
import org.eclipse.linuxtools.internal.docker.ui.launch.IBuildDockerImageLaunchConfigurationConstants;
import org.eclipse.linuxtools.internal.docker.ui.launch.IRunDockerImageLaunchConfigurationConstants;
import org.eclipse.linuxtools.internal.docker.ui.launch.LaunchConfigurationUtils;
import org.eclipse.linuxtools.internal.docker.ui.wizards.EditDockerConnection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Edit the selected {@link IDockerConnection}(s)
 */
public class EditDockerConnectionCommandHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) {
		final IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
		final IDockerConnection currentConnection = CommandUtils
				.getCurrentConnection(activePart);
		final String oldConnectionName = currentConnection.getName();
		final EditDockerConnection wizard = new EditDockerConnection(
				currentConnection);
		if (CommandUtils.openWizard(wizard,
				HandlerUtil.getActiveShell(event))) {
			// apply the changes to all launch configurations if needed
			final String newConnectionName = currentConnection.getName();
			if (!newConnectionName.equals(oldConnectionName)) {
				LaunchConfigurationUtils.updateLaunchConfigurations(
						IBuildDockerImageLaunchConfigurationConstants.CONFIG_TYPE_ID,
						IDockerImageBuildOptions.DOCKER_CONNECTION,
						oldConnectionName, newConnectionName);
				LaunchConfigurationUtils.updateLaunchConfigurations(
						IRunDockerImageLaunchConfigurationConstants.CONFIG_TYPE_ID,
						IRunDockerImageLaunchConfigurationConstants.CONNECTION_NAME,
						oldConnectionName, newConnectionName);
			}
		}
		return null;
	}

}
