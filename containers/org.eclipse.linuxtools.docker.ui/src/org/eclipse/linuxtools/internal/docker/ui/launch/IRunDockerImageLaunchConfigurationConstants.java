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
 * Constants used in the "Image Run" {@link ILaunchConfiguration}
 */
public interface IRunDockerImageLaunchConfigurationConstants {

	/**
	 * the launch id
	 */
	String CONFIG_TYPE_ID = "org.eclipse.linuxtools.docker.ui.runDockerImageLaunchConfigurationType"; //$NON-NLS-1$

	/** The date when the launch configuration was <strong>created</strong>. */
	String CREATION_DATE = "creationDate"; //$NON-NLS-1$

	String IMAGE_ID = "imageId"; //$NON-NLS-1$

	String IMAGE_NAME = "imageName"; //$NON-NLS-1$

	String CONTAINER_NAME = "containerName"; //$NON-NLS-1$

	String ENTRYPOINT = "entryPoint"; //$NON-NLS-1$

	String COMMAND = "command"; //$NON-NLS-1$

	String PUBLISH_ALL_PORTS = "publishAllPorts"; //$NON-NLS-1$

	String PUBLISHED_PORTS = "publishedPorts"; //$NON-NLS-1$

	String LINKS = "links"; //$NON-NLS-1$

	String INTERACTIVE = "interactive"; //$NON-NLS-1$

	String ALLOCATE_PSEUDO_CONSOLE = "allocatePseudoTTY"; //$NON-NLS-1$

	String AUTO_REMOVE = "autoRemove"; //$NON-NLS-1$

	String PRIVILEGED = "privileged"; //$NON-NLS-1$

	String DATA_VOLUMES = "volumes"; //$NON-NLS-1$

	String VOLUMES_FROM = "volumesFrom"; //$NON-NLS-1$

	String BINDS = "binds"; //$NON-NLS-1$

	String ENV_VARIABLES = "envVariables"; //$NON-NLS-1$

	String ENABLE_LIMITS = "enableLimits"; //$NON-NLS-1$

	String CPU_PRIORITY = "cpuPriority"; //$NON-NLS-1$

	String MEMORY_LIMIT = "memoryLimit"; //$NON-NLS-1$

	String CONNECTION_NAME = "connectionName"; //$NON-NLS-1$

	long MB = 1024l * 1024l;

}
