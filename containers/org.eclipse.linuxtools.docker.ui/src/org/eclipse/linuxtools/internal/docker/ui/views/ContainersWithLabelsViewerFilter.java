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

import java.util.Set;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.linuxtools.docker.core.IDockerContainer;

public class ContainersWithLabelsViewerFilter extends ViewerFilter {

	private Set<String> ids;

	public void setIds(Set<String> ids) {
		this.ids = ids;
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (ids == null)
			return true;
		if (element instanceof IDockerContainer) {
			final IDockerContainer container = (IDockerContainer) element;
			if (ids.contains(container.id())) {
				return true;
			}
			return false;
		}
		return true;
	}

}
