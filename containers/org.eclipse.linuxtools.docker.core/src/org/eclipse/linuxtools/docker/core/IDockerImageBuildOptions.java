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

package org.eclipse.linuxtools.docker.core;

/**
 * Name of options passed to the Docker daemon to build an {@link IDockerImage}
 *
 */
public interface IDockerImageBuildOptions {

	/** name of the connection to use to build the Image. */
	public final static String DOCKER_CONNECTION = "dockerConnection"; //$NON-NLS-1$

	/** repo and optionally tag of Image to build. */
	public final static String REPO_NAME = "repoName"; //$NON-NLS-1$

	/** quiet build option. */
	public static final String QUIET_BUILD = "quietBuild"; //$NON-NLS-1$

	/** no cache option. */
	public static final String NO_CACHE = "noCache"; //$NON-NLS-1$

	/** remove intermediate containers option on successful build. */
	public static final String RM_INTERMEDIATE_CONTAINERS = "rm"; //$NON-NLS-1$

	/** always remove intermediate containers option. */
	public static final String FORCE_RM_INTERMEDIATE_CONTAINERS = "forcerm"; //$NON-NLS-1$

}
