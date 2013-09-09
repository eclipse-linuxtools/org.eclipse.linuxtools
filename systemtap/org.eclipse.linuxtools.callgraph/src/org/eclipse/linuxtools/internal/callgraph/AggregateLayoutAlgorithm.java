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
package org.eclipse.linuxtools.internal.callgraph;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeSet;

import org.eclipse.zest.layouts.algorithms.GridLayoutAlgorithm;
import org.eclipse.zest.layouts.dataStructures.InternalNode;
import org.eclipse.zest.layouts.dataStructures.InternalRelationship;

/**
 * Calculates the size and location of a node when rendering the
 * Aggregate View. This avoids needing to design a Layout Algorithm
 * from scratch.
 */
public class AggregateLayoutAlgorithm extends GridLayoutAlgorithm{

	protected ArrayList<Long> list;
	protected Long totalTime;
	protected int graphWidth;


	/**
	 * Layout algorithm for the Aggregate View in Eclipse Callgraph, based on the GridLayoutAlgorithm in Zest.
	 * @param styles
	 * @param entries
	 * @param time
	 * @param width
	 */
	public AggregateLayoutAlgorithm(int styles, TreeSet<Entry<String, Long>> entries, Long time, int width){
		super(styles);

		list = new ArrayList<Long>();
		for (Entry<String, Long> ent : entries) {
			list.add(ent.getValue());
		}

		this.totalTime = time;
		this.graphWidth = width;
	}

	/**
	 * Called at the end of the layout algorithm -- change the size and colour
	 * of each node according to times called/total time
	 */
	@Override
	protected void postLayoutAlgorithm(InternalNode[] entitiesToLayout,
			InternalRelationship[] relationshipsToConsider) {
		final int minimumSize = 40;
		double xcursor = 0.0;
		double ycursor = 0.0;

		for (InternalNode sn : entitiesToLayout) {
			Long time = list.remove(0);
			double percent = (double) time / (double) totalTime;
			double snWidth = (sn.getInternalWidth() * percent) + minimumSize;
			double snHeight = (sn.getInternalHeight() * percent) + minimumSize;


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
