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
import org.eclipse.linuxtools.docker.core.IDockerImageBuildOptions;

/**
 * Constants used to pass values in the {@link ILaunchConfiguration} to build
 * Docker Images.
 * 
 */
public interface IBuildDockerImageLaunchConfigurationConstants
		extends IDockerImageBuildOptions {

	/**
	 * the launch id
	 */
	String CONFIG_TYPE_ID = "org.eclipse.linuxtools.docker.ui.buildDockerImageLaunchConfigurationType"; //$NON-NLS-1$

	/** the source path to build the Docker Image. */
	String SOURCE_PATH_LOCATION = "sourcePathLocation"; //$NON-NLS-1$

	/**
	 * marker to indicate if the location above is a workspace-relative
	 * location.
	 */
	String SOURCE_PATH_WORKSPACE_RELATIVE_LOCATION = "sourcePathWorkspaceRelativeLocation"; //$NON-NLS-1$

	/** the name of the dockerfile. */
	String DOCKERFILE_NAME = "dockerfileName"; //$NON-NLS-1$

	/** the path to the dockerfile. */
	String DOCKERFILE_PATH = "dockerfilePath"; //$NON-NLS-1$

	/**
	 * marker to indicate if the location above is a workspace-relative
	 * location.
	 */
	String DOCKERFILE_PATH_WORKSPACE_RELATIVE_LOCATION = "dockerfilePathWorkspaceRelativeLocation"; //$NON-NLS-1$

}
