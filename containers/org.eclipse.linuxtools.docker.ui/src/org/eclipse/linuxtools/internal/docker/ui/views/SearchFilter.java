/*******************************************************************************
 * Copyright (c) 2015 Red Hat.
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

/**
 * Basic filtering of table elements for table views.
 */
public class SearchFilter extends ViewerFilter {

	private String match;

	public void setMatch(String match) {
		this.match = match;
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement,
			Object element) {
		return match == null || match.isEmpty() || element.toString().contains(match);
	}
}
