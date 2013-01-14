/*******************************************************************************
 * Copyright (c) 2010-2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.threadprofiler.graphs;

import java.util.Iterator;

import org.eclipse.linuxtools.internal.threadprofiler.DataPoint;
import org.eclipse.swt.graphics.GC;

public class ThreadGraph extends GraphModel{

	private int tid;
	private DataPoint dp;
	private boolean empty;
	
	public ThreadGraph(String name, int tid) {
		super(name, "", 0, 0, 0);
		this.tid = tid;
		empty = false;
	}
 	
	public ThreadGraph(String name, String units, int x, int y, int type, int tid) {
		super(name, units, x, y, type);
 		this.tid = tid;
		empty = false;
 	}
 	
 	public int getTid() {
 		return tid;
 	}
	
	/**
	 * Returns true if this graph has data points and they are all empty
	 * 
	 * @return
	 */
	public boolean isEmpty() {
		return empty;
	}
	
	@Override
	public void draw(GC gc) {
		double increment = getXIncrement(gc);

		//Each thread should only have one buffer
		Iterator<DataPoint> buffer = data.get(0).getIterator();
		double xPos = this.getXOffset();
		empty = true;
		if (!buffer.hasNext())
			empty = false;
		double temp = 0;
		while (buffer.hasNext()) {
			DataPoint p = buffer.next();
			if (p.getType() == DataPoint.THREAD_ACTIVE) {
				temp += increment;
				empty = false;
			} else {
				if (temp != 0) {
					gc.drawLine((int) (xPos + 0.5), getYOffset(), (int) (xPos + temp + 0.5), getYOffset());
					xPos += temp;
					temp = 0;
				} else {
					xPos += increment;
				}
			}
		}
		gc.drawLine((int) (xPos + 0.5), getYOffset(), (int) (xPos + temp + 0.5), getYOffset());
	}

	public void addPoint() {
		dp = new DataPoint(0, 0, DataPoint.THREAD_ACTIVE);
	}
	
	public void tick() {
		//TODO: This method of updating requires passing around a bulky datapoint variable
		if (dp != null)
			add(dp, 0);
		else {
			add(new DataPoint(0, 0, DataPoint.THREAD_INACTIVE), 0);
		}
		dp = null;
 	}
 
 }