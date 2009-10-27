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
package org.eclipse.linuxtools.callgraph;

import java.util.ArrayList;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.eclipse.zest.layouts.algorithms.GridLayoutAlgorithm;
import org.eclipse.zest.layouts.dataStructures.InternalNode;
import org.eclipse.zest.layouts.dataStructures.InternalRelationship;

/**
 * Calculates the size and location of a node when rendering the
 * Aggregate View. This avoids needing to design a Layout Algorithm
 * from scratch.
 */
public class AggregateLayoutAlgorithm extends GridLayoutAlgorithm{
	
	protected ArrayList<Long> sortedAggregateTimes;
	protected Long totalTime;
	protected int graphWidth;
	protected Long endTime;
	
	public AggregateLayoutAlgorithm(int styles, TreeSet<Entry<String, Long>> entries, Long time, int width, long endTime){
		super(styles);
		
		this.sortedAggregateTimes = new ArrayList<Long>();
		for (Entry<String,Long> val : entries){
			this.sortedAggregateTimes.add(val.getValue());
		}
		
		this.totalTime = time;
		this.graphWidth = width;
		this.endTime = endTime;
	}
	
	//THIS METHOD OVERRIDES THE PARENT'S IMPLEMENTATION (WHICH IS EMPTY ANYWAYS)
	protected void postLayoutAlgorithm(InternalNode[] entitiesToLayout, InternalRelationship[] relationshipsToConsider) {
		Long time;
		final int minimumSize = 40;
		double percent;
		double xcursor = 0.0;
		double ycursor = 0.0;

		for (InternalNode sn : entitiesToLayout) {
			time = sortedAggregateTimes.remove(0);
			while (time < 0)
				time+=endTime;
			percent = (double) time / (double) totalTime;
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
