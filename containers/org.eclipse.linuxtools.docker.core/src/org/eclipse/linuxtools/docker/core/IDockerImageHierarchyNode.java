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

import org.eclipse.core.runtime.IAdaptable;

/**
 * The Hierarchy for a given {@link IDockerImage}
 */
public interface IDockerImageHierarchyNode extends IAdaptable {

	/**
	 * @return the selected {@link IDockerImage} or {@link IDockerContainer}.
	 */
	public Object getElement();

	/**
	 * @return the {@link IDockerImageHierarchyNode} corresponding to the parent
	 *         {@link IDockerImage} of the current element.
	 */
	public IDockerImageHierarchyNode getParent();

	/**
	 * @return the {@link IDockerImageHierarchyNode} of all {@link IDockerImage}
	 *         and {@link IDockerContainer} whose parent is the
	 *         {@link IDockerImage} associated with this
	 *         {@link IDockerImageHierarchyNode}
	 */
	public List<IDockerImageHierarchyNode> getChildren();

	/**
	 * @return the {@link IDockerImageHierarchyNode} of the {@link IDockerImage}
	 *         or {@link IDockerContainer} whose parent is the
	 *         {@link IDockerImage} associated with this
	 *         {@link IDockerImageHierarchyNode} and whose id matches the given
	 *         {@code id}, or <code>null</code> if none was found.
	 * @param id
	 *            the {@link IDockerContainer} or {@link IDockerImage} id to
	 *            look-up.
	 */
	public IDockerImageHierarchyNode getChild(String id);

	/**
	 * @return the root node of the resolved hierarchy
	 */
	public IDockerImageHierarchyNode getRoot();
}
