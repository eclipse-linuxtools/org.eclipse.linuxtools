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
import org.eclipse.linuxtools.docker.core.EnumDockerStatus;
import org.eclipse.linuxtools.docker.core.IDockerContainer;

/**
 * Filters out any {@link IDockerContainer} that is not in a running or paused
 * state.
 * 
 */
public class HideStoppedContainersViewerFilter extends ViewerFilter {

	@Override
	public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
		if(element instanceof IDockerContainer) {
			final IDockerContainer container = (IDockerContainer) element;
			final EnumDockerStatus containerStatus = EnumDockerStatus.fromStatusMessage(container.status());
			if (containerStatus == EnumDockerStatus.RUNNING
					|| containerStatus == EnumDockerStatus.PAUSED) {
				return true;
			}
			return false;
		}
		return true;
	}

}
