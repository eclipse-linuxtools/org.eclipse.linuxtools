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
            highestLevelNodes.put(lvl, new ArrayList<>());
        }
        highestLevelNodes.get(lvl).add(d.id);
    }

    public StapTreeListener(ScrollBar bar) {
        this.highestLevelOfExpansion=0;
        this.scrollbar = bar;
        highestLevelNodes = new HashMap<>();
    }
}
