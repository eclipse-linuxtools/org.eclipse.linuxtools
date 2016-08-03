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

import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.docker.core.IDockerImageHiearchyNode;

/**
 * Default implementation of the {@link IDockerImageHiearchyNode} interface.
 */
public class DockerImageHiearchyNode implements IDockerImageHiearchyNode {

	private final IDockerImage image;

	private IDockerImageHiearchyNode parent;

	private List<IDockerImageHiearchyNode> children;

	public DockerImageHiearchyNode(final IDockerImage image,
			final IDockerImageHiearchyNode parentImageHiearchyNode) {
		this.image = image;
		this.children = new ArrayList<>();
		this.parent = parentImageHiearchyNode;
		if (parent != null) {
			this.parent.getChildren().add(this);
		}
	}

	@Override
	public IDockerImage getImage() {
		return this.image;
	}

	@Override
	public IDockerImageHiearchyNode getParent() {
		return this.parent;
	}

	@Override
	public List<IDockerImageHiearchyNode> getChildren() {
		return this.children;
	}

	@Override
	public IDockerImageHiearchyNode getRoot() {
		if (this.parent == null) {
			return this;
		}
		return this.parent.getRoot();
	}
}
