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

package org.eclipse.linuxtools.docker.core;

/**
 * Name of options passed to the Docker daemon to build an {@link IDockerImage}
 *
 */
public interface IDockerImageBuildOptions {

	/** name of the connection to use to build the Image. */
	String DOCKER_CONNECTION = "dockerConnection"; //$NON-NLS-1$

	/** repo and optionally tag of Image to build. */
	String REPO_NAME = "repoName"; //$NON-NLS-1$

	/** dockerfile name. */
	String DOCKERFILE_NAME = "dockerfileName"; //$NON-NLS-1$

	/** quiet build option. */
	String QUIET_BUILD = "quietBuild"; //$NON-NLS-1$

	/** no cache option. */
	String NO_CACHE = "noCache"; //$NON-NLS-1$

	/** remove intermediate containers option on successful build. */
	String RM_INTERMEDIATE_CONTAINERS = "rm"; //$NON-NLS-1$

	/** always remove intermediate containers option. */
	String FORCE_RM_INTERMEDIATE_CONTAINERS = "forcerm"; //$NON-NLS-1$

}
