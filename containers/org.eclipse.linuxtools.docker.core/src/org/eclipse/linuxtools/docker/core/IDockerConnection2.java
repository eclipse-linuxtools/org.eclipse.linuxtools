/*******************************************************************************
 * Copyright (c) 2016, 2018 Red Hat.
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

	/**
	 * Retrieves the whole hierarchy for the given {@link IDockerImage}. This
	 * includes the path to all known parent images, along with all derived
	 * images based on the given {@code image}.
	 * 
	 * @param image
	 *            the {@link IDockerImage} for which the hierarchy should be
	 *            resolved
	 * @return the {@link IDockerImageHierarchyNode} as a node that can be
	 *         traversed.
	 */
	IDockerImageHierarchyNode resolveImageHierarchy(IDockerImage image);

	/**
	 * Retrieves the whole hierarchy for the given {@link IDockerContainer}.
	 * This includes the path to all known parent images and their derived
	 * {@link IDockerContainer}.
	 * 
	 * @param container
	 *            the {@link IDockerContainer} for which the hierarchy should be
	 *            resolved
	 * @return the {@link IDockerImageHierarchyNode} as a node that can be
	 *         traversed.
	 */
	IDockerImageHierarchyNode resolveImageHierarchy(IDockerContainer container);

	/**
	 * Retrieve an Image by id
	 *
	 * @param id
	 *            of Image
	 * @return the IDockerImage or null if not found
	 * @since 4.0
	 */
	IDockerImage getImage(String id);

}
