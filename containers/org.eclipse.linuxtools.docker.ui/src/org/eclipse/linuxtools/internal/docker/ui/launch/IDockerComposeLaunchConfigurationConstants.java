/*******************************************************************************
 * Copyright (c) 2015, 2016 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
