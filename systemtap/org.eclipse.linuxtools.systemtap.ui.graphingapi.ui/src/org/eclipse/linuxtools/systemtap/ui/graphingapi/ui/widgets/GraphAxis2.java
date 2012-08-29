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

package org.eclipse.linuxtools.systemtap.ui.graphingapi.ui.widgets;

import org.eclipse.linuxtools.systemtap.ui.graphingapi.ui.graphs.AGraph;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;



/**
 * An extension to GraphAxis.
 * @author Ryan Morse
 */
public class GraphAxis2 extends GraphAxis {
	public GraphAxis2(AGraph graph, String title, int tickCount, int type, Color color) {
		super(graph, title, tickCount, type&1);
		this.type = type;
		this.color = color;
	}

	public void calculateBounds() {
		x1 = graph.getXPadding();
		y1 = graph.getYPadding();
		x2 = graph.getSize().x-graph.getXPadding();
		y2 = graph.getSize().y-graph.getYPadding();
		locationX = graph.getXPadding();
		locationY = graph.getSize().y - graph.getYPadding();
		tickAmount = 0;

		switch(type&1) {
		case VERTICAL:
			widthX = 0;
			widthY = graph.getSize().y - (graph.getYPadding()<<1);
			tickIncrament = ((graph.getLocalYMax()-graph.getLocalYMin())/(double)tickCount);
			range = graph.getLocalYMax() - graph.getLocalYMin();
			localMin = graph.getLocalYMin();
			break;
		case HORIZONTAL:
			widthX = graph.getSize().x - (graph.getXPadding()<<1);
			widthY = 0;
			tickIncrament = ((graph.getLocalXMax()-graph.getLocalXMin())/(double)tickCount);
			range = graph.getLocalXMax() - graph.getLocalXMin();
			localMin = graph.getLocalXMin();
			break;
		}

		switch(type&3) {
		case ALIGN_BOTTOM:
			locationY = graph.getYPadding();
			y1 = graph.getSize().y-graph.getYPadding();
			y2a = graph.getSize().y - graph.getYPadding();
			x2a = (int)locationX;
			break;
		case ALIGN_TOP:
			y2 = graph.getYPadding();
			y2a = graph.getYPadding();
			x2a = (int)locationX;
			break;
		case ALIGN_LEFT:
			x2 = graph.getXPadding();
			y2a = (int)locationY;
			x2a = graph.getSize().x-graph.getXPadding();
			break;
		case ALIGN_RIGHT:
			locationX = graph.getSize().x-graph.getXPadding();
			x1 = graph.getSize().x-graph.getXPadding();
			y2a = (int)locationY;
			x2a = graph.getXPadding();
			break;
		}
	}

	protected void drawGridLine(GC gc, int x1, int y1, int x2, int y2) {
		if(HIDE_GRID_LINES != (HIDE_GRID_LINES&type))
			super.drawGridLine(gc, x1, y1, x2, y2);
	}
	
	protected void drawTick(GC gc, int x, int y, String val) {
		if(HIDE_TICKS != (HIDE_TICKS&type))
			super.drawTick(gc, x, y, val);
	}

	protected void drawTitle(GC gc) {
		if(HIDE_TITLE != (HIDE_TITLE&type))
			super.drawTitle(gc);
	}
	/*TODO: This should be reimplemented for viewing true values when normalized
	protected String getLabel(double val, int range) {
		if(UNNORMALIZED == (UNNORMALIZED&type))
			val = graph.unscaleValue(val);
		
		return super.getLabel(val, range);
	}
	*/
	public static final int ALIGN_LEFT = VERTICAL;			//0
	public static final int ALIGN_TOP = HORIZONTAL;			//1
	public static final int ALIGN_RIGHT = VERTICAL | 2;		//2
	public static final int ALIGN_BOTTOM = HORIZONTAL | 2;	//3
	
	public static final int HIDE_GRID_LINES = 4;
	public static final int HIDE_TITLE = 8;
	public static final int HIDE_TICKS = 16;
	public static final int UNNORMALIZED = 32;
}
