/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.graphingapi.ui.widgets;

import java.text.DecimalFormat;

import org.eclipse.linuxtools.internal.systemtap.graphingapi.ui.Localization;
import org.eclipse.linuxtools.systemtap.graphingapi.ui.graphs.AGraph;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;



/**
 * The Axis primitive, used to draw gridlines and axes on graphs.
 * @author Henry Hughes
 * @author Ryan Morse
 */
public class GraphAxis implements IGraphPrimitive {
	public GraphAxis(AGraph graph, String title, int tickCount, int type) {
		this.graph = graph;
		this.type = type&1;	//Ensure type matches one of the possible values
		this.tickCount = tickCount;
		this.title = title;
		this.color = graph.axisColor;
	}

	public void setTickCount(int count) {
		tickCount = count;
	}

	public int getType() {
		return type;
	}

	@Override
	public boolean isVisible() {
		return true;
	}

	/**
	 * Determines if the given point is inside this axis' bounds.
	 */
	@Override
	public boolean isUnder(Point loc) {
		if(type==VERTICAL && loc.x < graph.getXPadding() ||
			type==HORIZONTAL && loc.y > graph.getSize().y-graph.getYPadding())
			return true;
		return false;
	}

	@Override
	public void calculateBounds() {
		x1 = graph.getXPadding();
		y2 = graph.getSize().y-graph.getYPadding();
		locationX = graph.getXPadding();
		tickAmount = 0;

		if(type == HORIZONTAL) {
			locationY = graph.getYPadding();
			y1 = graph.getSize().y-graph.getYPadding();
			x2 = graph.getSize().x-graph.getXPadding();
			x2a = (int)locationX;
			y2a = graph.getSize().y-graph.getYPadding();

			widthX = graph.getSize().x - (graph.getXPadding()<<1);
			widthY = 0;
			tickIncrament = ((graph.getLocalXMax()-graph.getLocalXMin())/(double)tickCount);
			range = graph.getLocalXMax() - graph.getLocalXMin();
			localMin = graph.getLocalXMin();
		} else {
			locationY = graph.getSize().y - graph.getYPadding();
			y1 = graph.getYPadding();
			x2 = graph.getXPadding();
			x2a = graph.getSize().x-graph.getXPadding();
			y2a = (int)locationY;

			widthX = 0;
			widthY = graph.getSize().y - (graph.getYPadding()<<1);
			tickIncrament = ((graph.getLocalYMax()-graph.getLocalYMin())/(double)tickCount);
			range = graph.getLocalYMax() - graph.getLocalYMin();
			localMin = graph.getLocalYMin();
		}
	}

	/**
	 * Calculates the width, in pixels, of the input string.
	 * @param gc GC to use for the calculation.
	 * @param s String to calculate.
	 * @return Width of the string in pixels.
	 */
	protected int stringWidth(GC gc, String s) {
		int width = 0;
		for(int i=0; i<s.length(); i++)
			width += gc.getCharWidth(s.charAt(i));

		return width;
	}

	/**
	 * Draws the grid line for the given coordinates if grid lines are enabled.
	 */
	protected void drawGridLine(GC gc, int x1, int y1, int x2, int y2) {
		if(graph.showGrid) {
			gc.setLineStyle(SWT.LINE_DOT);
			gc.drawLine(x1,y1,x2,y2);
		}
	}

	/**
	 * Graphs the tick at the given location. Places the given string near the tick.
	 */
	protected void drawTick(GC gc, int x, int y, String val) {
		gc.setLineStyle(SWT.LINE_SOLID);
		int strWidth = stringWidth(gc, val);
		int strHeight = gc.getFontMetrics().getHeight();
		gc.drawLine(x, y, x+((HORIZONTAL==type) ? 0 : strWidth), y+((HORIZONTAL==type) ? -strHeight : 0));

		x -= (strWidth>>1);
		if(x < 0)
			x = 0;
		else if(x > graph.getSize().x-strWidth)
			x = graph.getSize().x-strWidth;

		y -= (strHeight>>1);
		if(y < 0)
			y = 0;
		else if(y > graph.getSize().y-strHeight)
			y = graph.getSize().y-strHeight;
		gc.drawText(val, x, y);
	}

	protected void drawTitle(GC gc) {
		//TODO: Implement this function.  Has to rotate text for vertical bars
		//http://www.java2s.com/Code/Java/SWT-JFace-Eclipse/Rotateandflipanimage.htm
	}

	/**
	 * Converts units on the input value using SI prefixes (1 million becomes 1M, etc)
	 */
	protected String getLabel(double val, int range) {
		int metric = 0;
		String pattern=".0"; //$NON-NLS-1$
		range /= tickCount;
		while(val > 1000) {
			val /= 1000;
			metric++;

			range /= 10;
			if(range < 1)
				pattern += "0"; //$NON-NLS-1$
		}

		DecimalFormat format = new DecimalFormat(pattern);
		return format.format(val) + PREFIXES[metric];
	}

	@Override
	public void paint(GC gc) {
		calculateBounds();

		Color foreground = gc.getForeground();
		gc.setForeground(color);

		gc.drawLine(x1, y1, x2, y2);
		drawTitle(gc);
		for(int i=0; i<=tickCount; i++) {
			drawGridLine(gc, (int)locationX, (int)locationY, (int)x2a, (int)y2a);
			drawTick(gc, (int)locationX, (int)y2a, getLabel(localMin+tickAmount, range));

			locationX += (widthX/(double)tickCount);
			x2a += (widthX/(double)tickCount);
			locationY -= (widthY/(double)tickCount);
			y2a -= (widthY/(double)tickCount);
			tickAmount += tickIncrament;

		}
		gc.setForeground(foreground);
	}

	public static final int VERTICAL = 0;
	public static final int HORIZONTAL = 1;
	//kilo, mega, giga, tera, peta, exa, zetta, yotta
	protected static final String[] PREFIXES = { "", Localization.getString("GraphAxis.Kilo"), Localization.getString("GraphAxis.Mega"), Localization.getString("GraphAxis.Giga"), Localization.getString("GraphAxis.Tera"), Localization.getString("GraphAxis.Peta"), Localization.getString("GraphAxis.Exa"), Localization.getString("GraphAxis.Zetta"), Localization.getString("GraphAxis.Yotta") } ; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$

	protected int type;
	protected int tickCount;
	protected final AGraph graph;
	protected Color color;
	@SuppressWarnings("unused")
	private String title;

	protected int x1, y1, x2, y2;
	protected int widthX, widthY, range, localMin;
	protected double locationX, locationY, x2a, y2a, tickAmount, tickIncrament;
}