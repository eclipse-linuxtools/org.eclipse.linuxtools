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

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.linuxtools.internal.threadprofiler.CircularPointBuffer;
import org.eclipse.linuxtools.internal.threadprofiler.DataPoint;
import org.eclipse.linuxtools.internal.threadprofiler.GraphColorScheme;
import org.eclipse.linuxtools.internal.threadprofiler.GraphCoordinateSystem;
import org.eclipse.swt.graphics.GC;

public abstract class GraphModel {
	
	//Constant Y scale
	public static final int CONSTANT_Y = 0;
	//Change Y scale as the maximum y point increases
	public static final int FLEXIBLE_Y = 1;
	/** Maximum buffer size. This affects the number of data points*/
	public static final int BUFFER_SIZE = 300;
	//Default height
	public static final int DEFAULT_HEIGHT = 100;
	/** Minimum separation between any two graduations on the axis */
	public static final int MIN_SEPARATION = 10;
	
	/** Max pixel size of the margins */
	public static final int MAX_MARGIN = 30;
	/** Min pixel size of the margins */
	public static final int MIN_MARGIN = 10;
	
	private final GraphCoordinateSystem coordinates;
	private final int flexible;
	private final String title;
	private final String units;
	private boolean changed;
	protected final ArrayList<CircularPointBuffer> data;
	protected GraphColorScheme colorScheme = GraphColorScheme.DEFAULT_SCHEME;
	
	private int height;
	
	private int maxY;
	//Height in pixels
	
	
	/**
	 * Creates a new graph model
	 * 
	 * 
	 */
	public GraphModel(String name, String units, int x, int y, int flex) {
		title = name;
		this.units = units;
		changed = false;
		coordinates = new GraphCoordinateSystem(x, y);
		coordinates.setYScale(0.2);
		coordinates.setLabel(DEFAULT_HEIGHT + units);
		flexible = flex;
		
		data = new ArrayList<CircularPointBuffer>();
		height = DEFAULT_HEIGHT;
		maxY = 100;
	}
	
	
	public void setMaxY(int max) {
		maxY = max;
	}
	
	public int getXOffset() {
		return (int) coordinates.getAxisToPixel().entries[0][2];
	}
	
	public int getYOffset() {
		return (int) coordinates.getAxisToPixel().entries[1][2];
	}


	/**
	 * 
	 * @return
	 * 			  First available point buffer if any is available, else null
	 */
	public Iterator<DataPoint> getPointBuffer() {
		if (data.size() > 1)
			return getPointBuffer(0);
		return null;
	}
	
	public Iterator<DataPoint> getPointBuffer(int index) {
		return data.get(index).getIterator();
	}

	public void add(DataPoint point, int subIndex) {
		if (subIndex == data.size()) {
			data.add(new CircularPointBuffer(BUFFER_SIZE));
		} else if (subIndex > data.size()) {
			//Do not create pointless buffers
			return;
		}
		if (flexible == FLEXIBLE_Y) {
			if (point.getY() > maxY ) {
				maxY = point.getY();
				coordinates.setLabel(maxY + " " + units);
				if ( point.getY() > height) {
					double newScale = (double) height/maxY;
					coordinates.setYScale(newScale);
				}
			}
		}
		data.get(subIndex).add(point);
		setChanged(true);
	}
	
	public DataPoint transform(DataPoint toTransform) {
		return coordinates.dataToPixel(toTransform);
	}
	
	public DataPoint transform(int x, int y, int z) {
		return coordinates.dataToPixel(new int[] {x, y, z});
	}

	public boolean isChanged() {
		return changed;
	}
	
	public void setChanged(boolean val) {
		changed = val;
	}

	public String getTitle() {
		return title;
	}

	public int getHeight() {
		return height;
	}
	
	public void setHeight(int value) {
		if (height == value)
			return;
		height = value;
		double newScale = (double) height/maxY;
		coordinates.setYScale(newScale);
	}
	
	public void setXScale(double value) {
		coordinates.setXScale(value);
	}


	/**
	 * Set offset -- has minimum value of MIN_SEPARATION
	 * @param value
	 */
	public void setYOffset(int value) {
		if (value < MIN_MARGIN)
			value = MIN_MARGIN;
		else if (value > getHeight() + MAX_MARGIN/2)
			value = getHeight() + MAX_MARGIN/2;
		coordinates.setYOffset(value);
	}
	
	/**
	 * Set offset -- has minimum value of MIN_SEPARATION
	 * @param value
	 */
	public void setXOffset(int value) {
		if (value < MIN_MARGIN)
			value = MIN_MARGIN;
		else if (value > MAX_MARGIN)
			value = MAX_MARGIN;
		coordinates.setXOffset(value);
	}


	public GraphCoordinateSystem getCoordinates() {
		return coordinates;
	}

	/**
	 * The main draw function -- implement this method to change
	 * the way things are graphed
	 * 
	 * @param gc
	 * @param graphCanvas
	 */
	public abstract void draw(GC gc);

	
	protected void drawLine(GC gc, DataPoint p1, DataPoint p2) {
		gc.drawLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
	}
	
	/**
	 * 
	 * @return
	 * 			  Number of data buffers in this model
	 */
	public int getBufferCount() {
		return data.size();
	}
	
	/**
	 * Draws the x-axis and sets up the graph's title.
	 * Also draws graduations for the x-axis
	 * 
	 * @param gc
	 * @param width
	 */
	protected void drawAxis(GC gc) {
		gc.setForeground(colorScheme.getFontColor());
		gc.drawText(getTitle(), getXOffset(), getYOffset() + 5, true);
		
		double xSeg = getXOffset();
		double increment = getXIncrement(gc);
		if (increment < MIN_SEPARATION)
			increment = MIN_SEPARATION;
		
		
		double endX = gc.getClipping().width - getXOffset();
		
		gc.setAlpha(255);
		gc.setLineWidth(2);
		gc.setForeground(colorScheme.getAxisColor());
		while (xSeg < endX) {
			gc.drawLine(round(xSeg), getYOffset(), round(xSeg), getYOffset() - getHeight());
			xSeg += increment;
		}
		gc.drawLine(getXOffset(), getYOffset(), round(xSeg), getYOffset());
	}
	
	protected double getXIncrement(GC gc) {
		double incr = (gc.getClipping().width - (double)2*getXOffset())/BUFFER_SIZE;
		if ( incr <= 0 )
			incr = 1;
		return incr;
	}
	
	protected double getYScale() {
		return coordinates.getYScale();
	}
	
	protected int round(double value) {
		return (value > 0.5) ? (int) value + 1 : (int) value;
	}

}
