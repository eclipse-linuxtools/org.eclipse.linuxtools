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

package org.eclipse.linuxtools.internal.docker.core;

import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.docker.core.IDockerImageHierarchyImageNode;
import org.eclipse.linuxtools.docker.core.IDockerImageHierarchyNode;

/**
 * Default {@link IDockerImageHierarchyImageNode} implementation
 */
public class DockerImageHierarchyImageNode extends DockerImageHierarchyNode
		implements IDockerImageHierarchyImageNode {

	private final IDockerImage image;

	public DockerImageHierarchyImageNode(final IDockerImage image,
			final IDockerImageHierarchyNode parentImageHiearchyNode) {
		super(parentImageHiearchyNode);
		this.image = image;
	}

	@Override
	public IDockerImage getElement() {
		return this.image;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(final Class<T> adapter) {
		if (IDockerImage.class.isAssignableFrom(adapter)) {
			return (T) this.image;
		}
		return super.getAdapter(adapter);
	}

}
