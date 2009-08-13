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
package org.eclipse.linuxtools.systemtap.localgui.graphing.treeviewer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.linuxtools.systemtap.localgui.graphing.StapData;
import org.eclipse.linuxtools.systemtap.localgui.graphing.StapGraph;

public class StapTreeContentProvider implements ITreeContentProvider{

	private StapGraph graph;
	
	@Override
	public Object[] getChildren(Object parentElement) {
		List<StapData> EMPTY = new ArrayList<StapData>();
		if (parentElement instanceof StapData) {
			StapData parent = ((StapData) parentElement);
			List<Integer> childrenIDs = parent.collapsedCallees;
			for (int val : childrenIDs) {
				if (graph.getData(val) != null) {
					EMPTY.add(graph.getData(val));
				}
			}
		}
		return EMPTY.toArray();
	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof StapData)
			return graph.getData(((StapData) element).caller);
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof StapData)
			return element == null ? false : 
				((StapData) element).callees.size() > 0;
		return false;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	@Override
	public void dispose() {	
		
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}
	
	public void setGraph(StapGraph graph) {
		this.graph = graph;
	}

}
