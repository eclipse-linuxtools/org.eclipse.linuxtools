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

import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * Constants used to pass values in the {@link ILaunchConfiguration} to build
 * Docker Images.
 * 
 */
public interface IBuildDockerImageLaunchConfigurationConstants {

	/** the source path to build the Docker Image. */
	public final static String SOURCE_PATH_LOCATION = "sourcePathLocation"; //$NON-NLS-1$

	/**
	 * marker to indicate if the location above is a workspace-relative
	 * location.
	 */
	public final static String SOURCE_PATH_WORKSPACE_RELATIVE_LOCATION = "sourcePathWorkspaceRelativeLocation"; //$NON-NLS-1$

	/** name of the connection to use to build the Image. */
	public final static String DOCKER_CONNECTION = "dockerConnection"; //$NON-NLS-1$

}
