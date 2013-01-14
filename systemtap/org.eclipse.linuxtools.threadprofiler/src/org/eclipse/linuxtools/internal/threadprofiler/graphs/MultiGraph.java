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

import org.eclipse.linuxtools.internal.threadprofiler.CircularPointBuffer;
import org.eclipse.linuxtools.internal.threadprofiler.DataPoint;
import org.eclipse.linuxtools.internal.threadprofiler.GraphPointBuffer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;

public class MultiGraph extends GraphModel {

	/** Width of lines/squares for legend symbols */
	private static final int LEGEND_WIDTH = 10;
	/** Extra space to leave between legend entries */
	private static final int LEGEND_SEPARATION = LEGEND_WIDTH*2;
	/** 1.5 times LEGEND_WIDTH */
	private static final int LEGEND_WIDTH_AND_HALF = LEGEND_WIDTH + LEGEND_WIDTH/2;
	
	public static final int GRAPH_STYLE_FILL = 0;
	public static final int GRAPH_STYLE_LINE = 1;
	private int defaultStyle = GRAPH_STYLE_FILL;
	private int bufferSize = BUFFER_SIZE;

	public MultiGraph(String name, String units, int x, int y, int type) {
		super(name, units, x, y, type);
	}
	
	public void setDefaultStyle(int style) {
		defaultStyle = style;
	}
	
	public void setDefaultSize(int size) {
		bufferSize= size;
	}

	@Override
	public void draw(GC gc) {
		Color color = colorScheme.defaultColor;
		Iterator<Color> it = colorScheme.getIterator();
		
		for (CircularPointBuffer b : data) {
			if (! (b instanceof GraphPointBuffer)) {
				continue;
			}
			if (it.hasNext())
				color = it.next();
			gc.setLineWidth(2);
			GraphPointBuffer buf = (GraphPointBuffer) b;
			
			switch (buf.getStyle()) {
			case GRAPH_STYLE_LINE :
				gc.setForeground(color);
				drawLineGraph(gc, buf.getIterator());
				break;
			case GRAPH_STYLE_FILL :
				gc.setBackground(color);
				drawFillPolygonGraph(gc, buf.getIterator());
				break;
			default :
				break;
			}
		}
		drawAxis(gc);
		drawLegend(gc);
	}
	
	private void drawLegend(GC gc) {
		int x = getXOffset() + gc.getFontMetrics().getAverageCharWidth() * getTitle().length() + LEGEND_SEPARATION + LEGEND_WIDTH_AND_HALF;
		
		Color color = colorScheme.defaultColor;
		Iterator<Color> it = colorScheme.getIterator();
		
		for (CircularPointBuffer b : data) {
			if (! (b instanceof GraphPointBuffer)) {
				continue;
			}
			if (it.hasNext())
				color = it.next(); 
			GraphPointBuffer buf = (GraphPointBuffer) b;
			
			switch(buf.getStyle()) {
			case GRAPH_STYLE_LINE :
				gc.setLineWidth(2);
				gc.setForeground(color);
				gc.drawLine(x, getYOffset() + LEGEND_WIDTH_AND_HALF, x + LEGEND_WIDTH, getYOffset() + LEGEND_WIDTH_AND_HALF);
				break;
			case GRAPH_STYLE_FILL :	
				gc.setBackground(color);
				gc.fillRectangle(x, getYOffset() + LEGEND_WIDTH, LEGEND_WIDTH, LEGEND_WIDTH);
				break;
			default :
				break;
			}
			
			gc.setForeground(colorScheme.getFontColor());
			gc.drawText(buf.getName(), x + LEGEND_WIDTH_AND_HALF, getYOffset() + LEGEND_WIDTH/2, true);
			
			x += gc.getFontMetrics().getAverageCharWidth() * buf.getName().length() + LEGEND_SEPARATION + LEGEND_WIDTH_AND_HALF;
		}
		
	}

	public void addBuffer(String name) {
		addBuffer(bufferSize, name, defaultStyle);
	}
	
	public void addBuffer(int bufferSize, String name) {
		addBuffer(bufferSize, name, defaultStyle);
	}
	
	public void addBuffer(String name, int graphStyle) {
		addBuffer(bufferSize, name, graphStyle);
	}
	
	public void addBuffer(int bufferSize, String name, int graphStyle) {
		data.add(new GraphPointBuffer(bufferSize, graphStyle, name));
	}

	protected void drawFillPolygonGraph(GC gc, Iterator<DataPoint> nextPointBuffer) {
		DataPoint pp = new DataPoint(getXOffset(), getYOffset(), 0);
		double xSeg = this.getXOffset();
		double increment = getXIncrement(gc);
		while (nextPointBuffer.hasNext()) {
			gc.setAlpha(170);
			DataPoint nextPoint = new DataPoint((int) (xSeg + 0.5), this.transform(nextPointBuffer.next()).getY(), 0);
			//Use old point, new point, and the corresponding intersections with the x-axis to fill
			gc.fillPolygon(new int[] {pp.getX(), pp.getY(), nextPoint.getX(), nextPoint.getY(), nextPoint.getX(), getYOffset(), pp.getX(), getYOffset()});
			xSeg += increment;
			pp = nextPoint;
    	}
	}
	
	protected void drawLineGraph(GC gc, Iterator<DataPoint> nextPointBuffer) {
		DataPoint pp = new DataPoint(getXOffset(), getYOffset(), 0);
		double xSeg = this.getXOffset();
		double increment = getXIncrement(gc);
		
		while (nextPointBuffer.hasNext()) {
			DataPoint nextPoint = new DataPoint((int) (xSeg + 0.5), this.transform(nextPointBuffer.next()).getY(), 0);
			xSeg += increment;
			drawLine(gc, pp, nextPoint);
			pp = nextPoint;
    	}
	}

}
