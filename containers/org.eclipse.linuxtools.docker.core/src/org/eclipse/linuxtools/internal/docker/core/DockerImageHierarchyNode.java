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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.linuxtools.docker.core.IDockerContainer;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.docker.core.IDockerImageHierarchyNode;

/**
 * Default implementation of the {@link IDockerImageHierarchyNode} interface.
 */
public abstract class DockerImageHierarchyNode
		implements IDockerImageHierarchyNode {

	private IDockerImageHierarchyNode parent;

	private List<IDockerImageHierarchyNode> children;

	public DockerImageHierarchyNode(
			final IDockerImageHierarchyNode parentImageHiearchyNode) {
		this.children = new ArrayList<>();
		this.parent = parentImageHiearchyNode;
		if (parent != null) {
			this.parent.getChildren().add(this);
		}
	}

	@Override
	public <T> T getAdapter(final Class<T> adapter) {
		return null;
	}

	@Override
	public IDockerImageHierarchyNode getParent() {
		return this.parent;
	}

	@Override
	public List<IDockerImageHierarchyNode> getChildren() {
		return this.children;
	}

	@Override
	public IDockerImageHierarchyNode getChild(final String id) {
		return this.children.stream().filter(n -> {
			if (n.getElement() instanceof IDockerImage) {
				return ((IDockerImage) n.getElement()).id().equals(id);
			} else {
				return ((IDockerContainer) n.getElement()).id().equals(id);
			}
		}).findFirst().orElse(null);
	}

	@Override
	public IDockerImageHierarchyNode getRoot() {
		if (this.parent == null) {
			return this;
		}
		return this.parent.getRoot();
	}

	@Override
	public String toString() {
		return getElement().toString();
	}
}
