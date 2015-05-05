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
import org.eclipse.linuxtools.docker.core.EnumDockerStatus;
import org.eclipse.linuxtools.docker.core.IDockerContainer;

/**
 * Filters out any {@link IDockerContainer} that is not in a running state.
 * @author xcoulon
 *
 */
public class HideStoppedContainersViewerFilter extends ViewerFilter {

	/**
	 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
		if(element instanceof IDockerContainer) {
			final IDockerContainer container = (IDockerContainer) element;
			final EnumDockerStatus containerStatus = EnumDockerStatus.fromStatusMessage(container.status());
			if(containerStatus == EnumDockerStatus.RUNNING) {
				return true;
			}
			return false;
		}
		return true;
	}

}
