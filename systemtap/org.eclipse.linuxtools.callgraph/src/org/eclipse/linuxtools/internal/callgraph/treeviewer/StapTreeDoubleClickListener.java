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

import java.util.Iterator;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.linuxtools.internal.callgraph.StapData;
import org.eclipse.linuxtools.internal.callgraph.StapGraph;

public class StapTreeDoubleClickListener implements IDoubleClickListener {

    private StapGraph graph;
    private TreeViewer viewer;

    public StapTreeDoubleClickListener(TreeViewer t , StapGraph g) {
        this.graph  = g;
        this.viewer = t;
    }

    @Override
    public void doubleClick(DoubleClickEvent event) {
        if (!(event.getSelection() instanceof IStructuredSelection)) {
            return;
        }
        IStructuredSelection selection = (IStructuredSelection) event.getSelection();
        if (selection.size() != 1) {
            return;
        }


        //Expand the current node in the tree viewer and on the graph
        for (Iterator<?> iterator = selection.iterator(); iterator.hasNext();) {
            StapData data = (StapData) iterator.next();
            viewer.collapseToLevel(data, 1);
            viewer.expandToLevel(data, 1);
            graph.setCollapseMode(true);
            graph.draw(data.id);
            graph.getNode(data.id).unhighlight();
        }

        graph.setFocus();
    }

}
