/*******************************************************************************
 * Copyright (c) 2008, 2018 Alexander Kurtakov.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Alexander Kurtakov - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.ui.editor.outline;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * Simple filter that only check whether the label contains the given string to
 * look for.
 */
public class ContainsFilter extends ViewerFilter {

	private String lookFor;

	public void setLookFor(String lookFor) {
		this.lookFor = lookFor;
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (!(viewer instanceof TreeViewer treeViewer)) {
			return true;
		}
		String currentLabel = ((ILabelProvider) treeViewer.getLabelProvider()).getText(element);
		if ((lookFor == null) || (currentLabel != null && currentLabel.contains(lookFor))) {
			return true;
		}
		return hasUnfilteredChild(treeViewer, element);
	}

	private boolean hasUnfilteredChild(TreeViewer viewer, Object element) {
		Object[] children = ((ITreeContentProvider) viewer.getContentProvider()).getChildren(element);
		for (Object child : children) {
			if (select(viewer, element, child)) {
				return true;
			}
		}
		return false;
	}
}
