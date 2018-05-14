/*******************************************************************************
 * Copyright (c) 2015, 2018 Red Hat.
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

package org.eclipse.linuxtools.internal.docker.ui.launch;

import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * Constants used to pass values in the {@link ILaunchConfiguration} to build
 * Docker Images.
 * 
 */
public interface IDockerComposeLaunchConfigurationConstants {

	/** name of the connection to use to build the Image. */
	String DOCKER_CONNECTION = "dockerConnection"; //$NON-NLS-1$

	/**
	 * the launch id
	 */
	String CONFIG_TYPE_ID = "org.eclipse.linuxtools.docker.ui.dockerComposeUpLaunchConfigurationType"; //$NON-NLS-1$

	/** the working directory to execute the 'docker compose' command. */
	String WORKING_DIR = "workingDir"; //$NON-NLS-1$

	/**
	 * marker to indicate if the {@code WORKING_DIR} is a workspace-relative
	 * location.
	 */
	String WORKING_DIR_WORKSPACE_RELATIVE_LOCATION = "workingDirWorkspaceRelativeLocation"; //$NON-NLS-1$

}
