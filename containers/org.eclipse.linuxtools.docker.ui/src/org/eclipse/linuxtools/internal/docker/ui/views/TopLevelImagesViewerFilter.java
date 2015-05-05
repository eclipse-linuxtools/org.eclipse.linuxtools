/*******************************************************************************
 * Copyright (c) 2014, 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.docker.ui.views;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.linuxtools.docker.core.IDockerImage;

/**
 * @author xcoulon
 *
 */
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
		}
		return true;
	}

}
