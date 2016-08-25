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

import org.eclipse.linuxtools.docker.core.IDockerContainer;
import org.eclipse.linuxtools.docker.core.IDockerImageHierarchyContainerNode;
import org.eclipse.linuxtools.docker.core.IDockerImageHierarchyNode;

/**
 * Default {@link IDockerImageHierarchyContainerNode} implementation
 */
public class DockerImageHierarchyContainerNode extends DockerImageHierarchyNode
		implements IDockerImageHierarchyContainerNode {

	private final IDockerContainer container;

	public DockerImageHierarchyContainerNode(final IDockerContainer container,
			final IDockerImageHierarchyNode parentImageHiearchyNode) {
		super(parentImageHiearchyNode);
		this.container = container;
	}

	@Override
	public IDockerContainer getElement() {
		return this.container;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(final Class<T> adapter) {
		if (IDockerContainer.class.isAssignableFrom(adapter)) {
			return (T) this.container;
		}
		return super.getAdapter(adapter);
	}

}
