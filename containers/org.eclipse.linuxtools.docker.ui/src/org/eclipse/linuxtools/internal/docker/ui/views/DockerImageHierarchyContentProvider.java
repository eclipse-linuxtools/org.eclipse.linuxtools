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

package org.eclipse.linuxtools.internal.docker.ui.views;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.linuxtools.docker.core.IDockerImageHierarchyNode;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerImageHierarchyView.DockerImageHiearchy;


/**
 * The {@link ITreeContentProvider} implementation for the
 * {@link DockerImageHierarchyView}
 */
public class DockerImageHierarchyContentProvider
		implements ITreeContentProvider {

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof DockerImageHiearchy) {
			final DockerImageHiearchy dockerImageHiearchy = (DockerImageHiearchy) inputElement;
			return new Object[] { dockerImageHiearchy.getRoot() };
		} else if (inputElement instanceof IDockerImageHierarchyNode) {
			final IDockerImageHierarchyNode imageHiearchyNode = (IDockerImageHierarchyNode) inputElement;
			if (imageHiearchyNode.getChildren() != null) {
				return imageHiearchyNode.getChildren().toArray();
			}
		}
		return null;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		return getElements(parentElement);
	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof IDockerImageHierarchyNode) {
			final IDockerImageHierarchyNode imageHiearchyNode = (IDockerImageHierarchyNode) element;
			return imageHiearchyNode.getParent();
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof IDockerImageHierarchyNode) {
			final IDockerImageHierarchyNode imageHiearchyNode = (IDockerImageHierarchyNode) element;
			return !imageHiearchyNode.getChildren().isEmpty();
		}
		return false;
	}

	

}
