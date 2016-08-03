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

import java.util.List;

/**
 * The Hierarchy for a given {@link IDockerImage}
 */
public interface IDockerImageHiearchyNode {

	/**
	 * @return the selected {@link IDockerImage}
	 */
	public IDockerImage getImage();

	/**
	 * @return the {@link IDockerImageHiearchyNode} of the parent
	 *         {@link IDockerImage}
	 */
	public IDockerImageHiearchyNode getParent();

	/**
	 * @return the {@link IDockerImageHiearchyNode} of all {@link IDockerImage}
	 *         whose parent is the {@link IDockerImage} associated with this
	 *         {@link IDockerImageHiearchyNode}
	 */
	public List<IDockerImageHiearchyNode> getChildren();

	/**
	 * @return the root node of the resolved hierarchy
	 */
	public IDockerImageHiearchyNode getRoot();

}
