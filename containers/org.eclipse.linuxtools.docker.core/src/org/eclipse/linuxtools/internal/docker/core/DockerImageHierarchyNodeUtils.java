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

package org.eclipse.linuxtools.internal.docker.core;

import java.util.List;

import org.eclipse.linuxtools.docker.core.IDockerContainer;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.docker.core.IDockerImageHierarchyNode;

/**
 * Utility class to resolve {@link IDockerImageHierarchyNode}
 */
public class DockerImageHierarchyNodeUtils {

	public static IDockerImageHierarchyNode resolveImageHierarchy(
			final List<IDockerImage> images,
			final List<IDockerContainer> containers, final IDockerImage image) {
		// recursively find all parents and build associated
		// IDockerImageHierarchyNode instances
		final IDockerImageHierarchyNode parentImageNode = getImageParentImageNode(
				images, image.parentId());
		return getDockerImageHierarchyNode(images, containers, image,
				parentImageNode);
	}

	public static IDockerImageHierarchyNode resolveImageHierarchy(
			final List<IDockerImage> images,
			final IDockerContainer container) {
		final IDockerImageHierarchyNode parentImageNode = getContainerParentImageNode(
				images, container.image());
		final DockerImageHierarchyNode dockerImageHiearchyNode = getDockerImageHierarchyNode(
				container, parentImageNode);
		return dockerImageHiearchyNode;
	}

	/**
	 * Resolves the parent {@link IDockerImageHierarchyNode} for an
	 * {@link IDockerImage}.
	 * 
	 * @param images
	 *            all existing {@link IDockerImage}
	 * @param parentImageId
	 *            the id of the parent {@link IDockerImage} to look-up
	 * @return the {@link IDockerImageHierarchyNode} corresponding to the parent
	 *         {@link IDockerImage} to look-up or <code>null</code> if none was
	 *         found.
	 */
	private static IDockerImageHierarchyNode getImageParentImageNode(
			final List<IDockerImage> images, final String parentImageId) {
		// recursively find all parents and build associated
		// IDockerImageHierarchyNode instances
		return images.stream().filter(image -> image.id().equals(parentImageId))
				// parent image found: get its own parent image hierarchy
				.map(parentImage -> new DockerImageHierarchyImageNode(
						parentImage,
						getImageParentImageNode(images,
								parentImage.parentId())))
				.findFirst()
				// no parent image found: stop here.
				.orElse(null);
	}

	/**
	 * Resolves the parent {@link IDockerImageHierarchyNode} for an
	 * {@link IDockerContainer}.
	 * 
	 * @param images
	 *            all existing {@link IDockerImage}
	 * @param parentImageName
	 *            the name of the parent {@link IDockerImage} to look-up
	 * @return the {@link IDockerImageHierarchyNode} corresponding to the parent
	 *         {@link IDockerImage} to look-up or <code>null</code> if none was
	 *         found.
	 */
	private static IDockerImageHierarchyNode getContainerParentImageNode(
			final List<IDockerImage> images, final String parentImageName) {
		// recursively find all parents and build associated
		// IDockerImageHierarchyNode instances
		return images.stream()
				.filter(image -> image.repoTags().contains(parentImageName))
				// parent image found: get its own parent image hierarchy
				.map(parentImage -> new DockerImageHierarchyImageNode(
						parentImage,
						getImageParentImageNode(images, parentImage.parentId())))
				.findFirst()
				// no parent image found: stop here.
				.orElse(null);
	}

	private static DockerImageHierarchyNode getDockerImageHierarchyNode(
			final List<IDockerImage> images,
			final List<IDockerContainer> containers, final IDockerImage image,
			final IDockerImageHierarchyNode parentImageNode) {
		final DockerImageHierarchyNode imageNode = new DockerImageHierarchyImageNode(
				image, parentImageNode);
		// also includes all children images/containers, recursively
		resolveChildrenImageNodes(images, containers, image.id(),
				image.repoTags(), imageNode);
		return imageNode;
	}

	private static void resolveChildrenImageNodes(
			final List<IDockerImage> images,
			final List<IDockerContainer> containers, final String imageId,
			final List<String> imageRepoTags,
			final IDockerImageHierarchyNode parentNode) {
		// recursively find all parents and build associated
		// IDockerImageHierarchyNode instances
		images.stream().filter(image -> image.parentId() != null
				&& image.parentId().equals(imageId))
				// use the flatMap below to duplicate images that have multiple repos
				//.flatMap(image -> DockerImage.duplicateImageByRepo(image))
				.forEach(image -> {
					final DockerImageHierarchyNode childNode = new DockerImageHierarchyImageNode(
							image, parentNode);
					resolveChildrenImageNodes(images, containers, image.id(),
							image.repoTags(),
							childNode);
				});
		containers.stream()
				.filter(container -> container.image() != null
						&& imageRepoTags.contains(container.image()))
				.forEach(container -> new DockerImageHierarchyContainerNode(
						container,
						parentNode));
	}

	private static DockerImageHierarchyNode getDockerImageHierarchyNode(
			final IDockerContainer container,
			final IDockerImageHierarchyNode parentImageNode) {
		// recursively find all parents and build associated
		// IDockerImageHierarchyNode instances
		final DockerImageHierarchyNode containerNode = new DockerImageHierarchyContainerNode(
				container, parentImageNode);
		// there's no children images/containers for a container, so let's just
		// return the node
		return containerNode;
	}

}
