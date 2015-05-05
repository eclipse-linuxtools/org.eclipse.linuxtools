/*******************************************************************************
 * Copyright (c) 2014 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.ui.views;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.linuxtools.docker.core.IDockerImage;

public class ImageComparator extends ViewerComparator {

	private final static int UP = 1;

	private int column;
	private int direction;

	public ImageComparator(int column) {
		this.column = column;
		this.direction = UP;
	}

	public void setColumn(int newColumn) {
		if (column != newColumn)
			direction = UP;
		column = newColumn;
	}

	public int getColumn() {
		return column;
	}

	public void reverseDirection() {
		direction *= -1;
	}

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		if (!(e1 instanceof IDockerImage) || !(e2 instanceof IDockerImage))
			return 0;

		int tmp = compareByColumn(viewer, (IDockerImage) e1, (IDockerImage) e2);
		return tmp * direction;
	}

	private int compareByColumn(Viewer viewer, IDockerImage e1, IDockerImage e2) {
		ImageViewLabelAndContentProvider provider = (ImageViewLabelAndContentProvider) ((TableViewer) viewer)
				.getContentProvider();
		Object s1 = provider.getColumnCompareObject(e1, column);
		Object s2 = provider.getColumnCompareObject(e2, column);
		if (s1 instanceof String)
			return ((String) s1).compareToIgnoreCase((String) s2);
		else if (s1 instanceof Long) {
			return ((Long) s1).compareTo((Long) s2);
		} else
			return 0; // don't know
	}
}
