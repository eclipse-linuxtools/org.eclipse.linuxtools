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
package org.eclipse.linuxtools.internal.docker.ui;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Composite;

public class ColoredTableViewer extends TableViewer {

	private List<Object> currSortedChildren;

	public ColoredTableViewer(Composite parent, int style) {
		super(parent, style);
	}

	@Override
	protected Object[] getSortedChildren(Object parent) {
		Object[] sortedChildren = super.getSortedChildren(parent);
		currSortedChildren = Arrays.asList(sortedChildren);
		return sortedChildren;
	}

	public List<Object> getCurrSortedChildren() {
		return currSortedChildren;
	}

}
