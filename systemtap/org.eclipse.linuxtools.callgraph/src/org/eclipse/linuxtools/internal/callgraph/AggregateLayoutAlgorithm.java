/*******************************************************************************
 * Copyright (c) 2009, 2026 Red Hat, Inc.
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
package org.eclipse.linuxtools.internal.callgraph;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeSet;

import org.eclipse.zest.layouts.algorithms.GridLayoutAlgorithm;
import org.eclipse.zest.layouts.dataStructures.DisplayIndependentRectangle;
import org.eclipse.zest.layouts.interfaces.EntityLayout;

/**
 * Calculates the size and location of a node when rendering the
 * Aggregate View. This avoids needing to design a Layout Algorithm
 * from scratch.
 */
public class AggregateLayoutAlgorithm extends GridLayoutAlgorithm {

    private final List<Long> list;
    private final Long totalTime;
    private final int graphWidth;


    /**
     * Layout algorithm for the Aggregate View in Eclipse Callgraph, based on the GridLayoutAlgorithm in Zest.
     * @param entries
     * @param time
     * @param width
     */
    public AggregateLayoutAlgorithm(TreeSet<Entry<String, Long>> entries, Long time, int width){
        setResizing(true);

        list = new ArrayList<>();
        for (Entry<String, Long> ent : entries) {
            list.add(ent.getValue());
        }

        this.totalTime = time;
        this.graphWidth = width;
    }

    /**
     * Runs the grid layout, then changes the size and location of each node
     * according to times called/total time
     */
    @Override
    protected synchronized void applyLayoutInternal(EntityLayout[] entitiesToLayout,
            DisplayIndependentRectangle bounds) {
        super.applyLayoutInternal(entitiesToLayout, bounds);
        final int minimumSize = 40;
        double xcursor = 0.0;
        double ycursor = 0.0;

        for (int i = 0; i < entitiesToLayout.length; i++) {
            EntityLayout sn = entitiesToLayout[i];
            long time = list.get(i);
            double percent = (double) time / (double) totalTime;
            double snWidth = (sn.getSize().width * percent) + minimumSize;
            double snHeight = (sn.getSize().height * percent) + minimumSize;


            sn.setSize(snWidth, snHeight);
            if (xcursor + snWidth > graphWidth) {
                //reaching the end of row, move to lower column
                ycursor += snHeight;
                xcursor = 0;
                sn.setLocation(xcursor, ycursor);
            } else {
                sn.setLocation(xcursor, ycursor);
                xcursor += snWidth;
            }
        }
    }

}
