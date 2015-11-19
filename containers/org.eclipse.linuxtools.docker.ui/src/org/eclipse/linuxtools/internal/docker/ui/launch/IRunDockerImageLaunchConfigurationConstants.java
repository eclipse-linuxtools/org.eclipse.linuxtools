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
 * Constants used in the "Image Run" {@link ILaunchConfiguration}
 */
public interface IRunDockerImageLaunchConfigurationConstants {

	public static final String IMAGE_NAME = "imageName"; //$NON-NLS-1$

	public static final String CONTAINER_NAME = "containerName"; //$NON-NLS-1$

	public static final String ENTRYPOINT = "entryPoint"; //$NON-NLS-1$

	public static final String COMMAND = "command"; //$NON-NLS-1$

	public static final String PUBLISH_ALL_PORTS = "publishAllPorts"; //$NON-NLS-1$

	public static final String PUBLISHED_PORTS = "publishedPorts"; //$NON-NLS-1$

	public static final String LINKS = "links"; //$NON-NLS-1$

	public static final String INTERACTIVE = "interactive"; //$NON-NLS-1$

	public static final String ALLOCATE_PSEUDO_CONSOLE = "allocatePseudoTTY"; //$NON-NLS-1$

	public static final String AUTO_REMOVE = "autoRemove"; //$NON-NLS-1$

	public static final String DATA_VOLUME = "volumes"; //$NON-NLS-1$

	public static final String VOLUMES_FROM = "volumesFrom"; //$NON-NLS-1$

	public static final String BINDS = "binds"; //$NON-NLS-1$

	public static final String ENV_VARIABLES = "envVariables"; //$NON-NLS-1$

	public static final String ENABLE_LIMITS = "enableLimits"; //$NON-NLS-1$

	public static final String CPU_PRIORITY = "cpuPriority"; //$NON-NLS-1$

	public static final String MEMORY_LIMIT = "memoryLimit"; //$NON-NLS-1$

	public static final String CONNECTION_NAME = "connectionName"; //$NON-NLS-1$

}
