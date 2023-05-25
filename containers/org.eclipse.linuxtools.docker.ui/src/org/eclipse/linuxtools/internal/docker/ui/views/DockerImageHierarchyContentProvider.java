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
		if (inputElement instanceof DockerImageHiearchy dockerImageHiearchy) {
			return new Object[] { dockerImageHiearchy.getRoot() };
		} else if (inputElement instanceof IDockerImageHierarchyNode imageHiearchyNode) {
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
		if (element instanceof IDockerImageHierarchyNode imageHiearchyNode) {
			return imageHiearchyNode.getParent();
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof IDockerImageHierarchyNode imageHiearchyNode) {
			return !imageHiearchyNode.getChildren().isEmpty();
		}
		return false;
	}

	

}
