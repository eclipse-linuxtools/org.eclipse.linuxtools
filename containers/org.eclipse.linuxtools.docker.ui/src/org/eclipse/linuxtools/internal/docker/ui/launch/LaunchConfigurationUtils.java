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

import static org.eclipse.linuxtools.internal.docker.ui.launch.IRunDockerImageLaunchConfigurationConstants.COMMAND;
import static org.eclipse.linuxtools.internal.docker.ui.launch.IRunDockerImageLaunchConfigurationConstants.CONTAINER_NAME;
import static org.eclipse.linuxtools.internal.docker.ui.launch.IRunDockerImageLaunchConfigurationConstants.IMAGE_NAME;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.linuxtools.docker.core.IDockerContainer;
import org.eclipse.linuxtools.docker.ui.Activator;

/**
 * Utility class to manage {@link ILaunchConfiguration}
 */
public class LaunchConfigurationUtils {

	public static final String RUN_IMAGE_CONFIGURATION_TYPE = "org.eclipse.linuxtools.docker.ui.runDockerImageLaunchConfigurationType"; //$NON-NLS-1$

	/**
	 * Private constructor for this utility class.
	 */
	private LaunchConfigurationUtils() {
		// empty
	}

	public static ILaunchConfiguration saveLaunchConfiguration(
			final IDockerContainer container) {
		try {
			final ILaunchManager manager = DebugPlugin.getDefault()
					.getLaunchManager();
			final String configurationName = manager
					.generateLaunchConfigurationName(container.name());
			final ILaunchConfigurationType type = manager
					.getLaunchConfigurationType(RUN_IMAGE_CONFIGURATION_TYPE);
			final ILaunchConfigurationWorkingCopy workingCopy = type
					.newInstance(null, configurationName);
			workingCopy.setAttribute(IMAGE_NAME, container.image());
			workingCopy.setAttribute(CONTAINER_NAME, container.name());
			workingCopy.setAttribute(COMMAND, container.command());

		} catch (CoreException e) {
			Activator.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
					LaunchMessages.getString(
							"RunDockerImageLaunchConfiguration.creation.failure"), //$NON-NLS-1$
					e));
		}
		return null;
	}

	public static ILaunchConfiguration getLaunchConfiguration(
			final ILaunchConfigurationType type, final String name)
					throws CoreException {
		final ILaunchManager manager = DebugPlugin.getDefault()
				.getLaunchManager();
		for (ILaunchConfiguration launchConfiguration : manager
				.getLaunchConfigurations(type)) {
			if (launchConfiguration.getName().equals(name)) {
				return launchConfiguration;
			}
		}
		return null;
	}

}
