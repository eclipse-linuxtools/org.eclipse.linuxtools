/*******************************************************************************
 * Copyright (c) 2014, 2018 Red Hat.
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

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.docker.core.IDockerImageHierarchyNode;

public class TopLevelImagesViewerFilter extends ViewerFilter {

	/**
	 * Default constructor
	 */
	public TopLevelImagesViewerFilter() {
		super();
	}

	/**
	 * @return {@code false} when the given element is a {@link IDockerImage} which is either intermediate or dangling. Returns {@code true} otherwise.
	 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if(element instanceof IDockerImage) {
			return !((IDockerImage)element).isDangling() || !((IDockerImage)element).isIntermediateImage();
		} else if (element instanceof IDockerImageHierarchyNode) {
			return select(viewer, parentElement,
					((IDockerImageHierarchyNode) element).getElement());
		}
		return true;
	}

}
