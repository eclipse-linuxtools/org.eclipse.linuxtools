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

/**
 * @author xcoulon
 *
 */
public class IntermediateImagesViewerFilter extends ViewerFilter {

	/**
	 * Default Constructor
	 */
	public IntermediateImagesViewerFilter() {
		super();
	}

	/**
	 * @return {@code false} when the given element is an {@link IDockerImage}
	 *         and it is an intermediate image. Returns {@code true} otherwise.
	 * 
	 * @see IDockerImage#isIntermediateImage()
	 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer,
	 *      java.lang.Object, java.lang.Object)
	 */
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if(element instanceof IDockerImage) {
			return !((IDockerImage)element).isIntermediateImage();
		}
		return true;
	}

}
