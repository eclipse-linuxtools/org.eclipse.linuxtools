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
package org.eclipse.linuxtools.docker.core;

public interface IDockerConnection2 {

	/**
	 * Get the default progress handler for pulling images.
	 * 
	 * @param image
	 *            name of image being pulled
	 * @return progress handler
	 * @since 2.1
	 */
	IDockerProgressHandler getDefaultPullImageProgressHandler(String image);

	/**
	 * Get the default progress handler for pushing images.
	 * 
	 * @param image
	 *            name of image being pushed
	 * @return progress handler
	 * @since 2.1
	 */
	IDockerProgressHandler getDefaultPushImageProgressHandler(String image);

	/**
	 * Get the default progress handler for building images.
	 * 
	 * @param image
	 *            name of image being built
	 * @param lines
	 *            number of lines in Dockerfile
	 * @return progress handler
	 * @since 2.1
	 */
	IDockerProgressHandler getDefaultBuildImageProgressHandler(String image,
			int lines);

}
