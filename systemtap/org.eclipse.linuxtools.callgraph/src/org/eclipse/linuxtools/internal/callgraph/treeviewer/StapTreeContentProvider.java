/*******************************************************************************
 * Copyright (c) 2009, 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.callgraph.treeviewer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.linuxtools.internal.callgraph.StapData;
import org.eclipse.linuxtools.internal.callgraph.StapGraph;

public class StapTreeContentProvider implements ITreeContentProvider{

    private StapGraph graph;

    @Override
    public Object[] getChildren(Object parentElement) {
        List<StapData> empty = new ArrayList<>();
        if (parentElement instanceof StapData) {
            StapData parent = ((StapData) parentElement);
            List<Integer> childrenIDs = parent.collapsedChildren;
            for (int val : childrenIDs) {
                if (graph.getNodeData(val) != null) {
                    empty.add(graph.getNodeData(val));
                }
            }
        }
        return empty.toArray();
    }

    @Override
    public Object getParent(Object element) {
        if (element instanceof StapData) {
            return graph.getNodeData(((StapData) element).collapsedParent);
        }
        return null;
    }

    @Override
    public boolean hasChildren(Object element) {
        if (element instanceof StapData) {
            return ((StapData) element).children.size() > 0;
        }
        return false;
    }

    @Override
    public Object[] getElements(Object inputElement) {
        return getChildren(inputElement);
    }

    @Override
    public void dispose() {
        //Empty
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        //Empty
    }

    public void setGraph(StapGraph graph) {
        this.graph = graph;
    }

}
