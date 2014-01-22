/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.callgraph.treeviewer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.linuxtools.internal.callgraph.StapData;
import org.eclipse.swt.widgets.ScrollBar;

public class StapTreeListener implements ITreeViewerListener{
	private static final int INCREMENT = 15;

	private int highestLevelOfExpansion;
	private ScrollBar scrollbar;
	private HashMap<Integer, List<Integer>> highestLevelNodes;
	//Level of recursion, list of nodes at that level currently displayed in tree


	/**
	 * Autoscroll the horizontal scrollbar when there is a collapse event.
	 *
	 */
	@Override
	public void treeCollapsed(TreeExpansionEvent event) {
		StapData data = (StapData) event.getElement();
		if (highestLevelNodes.get(highestLevelOfExpansion) != null
				&& highestLevelNodes.get(highestLevelOfExpansion).remove((Integer) data.id)) {
					scrollbar.setSelection(scrollbar.getSelection() - INCREMENT);
					highestLevelOfExpansion--;
		}

	}

	/**
	 * Autoscroll the horizontal scrollbar when there is an expand event.
	 *
	 */
	@Override
	public void treeExpanded(TreeExpansionEvent event) {
		StapData d = ((StapData) event.getElement());
		if (d.levelOfRecursion > highestLevelOfExpansion) {
			scrollbar.setSelection(scrollbar.getSelection() + INCREMENT);
			highestLevelOfExpansion = ((StapData) event.getElement()).levelOfRecursion;
		}

		int lvl = d.levelOfRecursion;
		if (highestLevelNodes.get(lvl) == null) {
			highestLevelNodes.put(lvl, new ArrayList<Integer>());
		}
		highestLevelNodes.get(lvl).add(d.id);
	}

	public StapTreeListener(ScrollBar bar) {
		this.highestLevelOfExpansion=0;
		this.scrollbar = bar;
		highestLevelNodes = new HashMap<>();
	}
}
