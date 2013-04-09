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

import java.util.Arrays;

import org.eclipse.linuxtools.internal.systemtap.graphingapi.ui.Localization;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;



/**
 * A legend primitive for the Graphing system. Used to display a list of
 * all the series that are on this graph, and to change the current axis on a
 * normalized multi-series graph.
 * @author Jeff Briggs
 * @author Ryan Morse
 */
public class GraphLegend implements IGraphPrimitive {
	public GraphLegend(GraphCanvas graph, String[] keysPassed, Color[] colorsPassed) {
		this.graph = graph;
		colors = Arrays.copyOf(colorsPassed, colorsPassed.length);
		keys = Arrays.copyOf(keysPassed, keysPassed.length);
		bounds = new Rectangle[keys.length];

		width = 0;
		height = 0;
	}

	@Override
	public boolean isVisible() {
		return true;
	}

	@Override
	public void calculateBounds() {
		x = graph.getSize().x - width;
		y = 0;
	}

	private void getSize(GC gc) {
		textHeight = gc.getFontMetrics().getHeight();
		height = textHeight * (keys.length + 1) + (BORDER<<1);

		for (int i=0; i<TITLE.length(); i++)
			width += gc.getCharWidth(TITLE.charAt(i));

		int currWidth;
		for(int i=0; i<keys.length; i++) {
			currWidth = 0;
			for (int j=0; j<keys[i].length(); j++)
				currWidth += gc.getCharWidth(keys[i].charAt(j));

			if (currWidth > width)
				width = currWidth;
		}
		width += BOX_SIZE + 3*BORDER;
	}

	@Override
	public boolean isUnder(Point loc) {
		if(loc.x >=x && loc.y >= y && loc.x <= x+width && loc.y <= y+height)
			return true;
		return false;
	}

	@Override
	public void paint(GC gc) {
		if(width == 0 || height ==0)
			getSize(gc);
		calculateBounds();

		Color temp = gc.getForeground();
		gc.setForeground(graph.axisColor);
		gc.drawRectangle(x, y, width, height);
		gc.fillRectangle(x+1, y+1, width-1, height-1);
		gc.drawText(TITLE, x+BORDER, y+BORDER);

		for (int i=0; i<keys.length; i++) {
			gc.setForeground(colors[i]);

			bounds[i] = new Rectangle(x+BORDER, y+BORDER+((i+1)*textHeight), BOX_SIZE, BOX_SIZE);
			gc.fillGradientRectangle(bounds[i].x, bounds[i].y, BOX_SIZE, BOX_SIZE, true);

			gc.setForeground(graph.axisColor);
			gc.drawText(keys[i], x+(BORDER<<1)+BOX_SIZE, bounds[i].y);
		}

		gc.setForeground(temp);
	}

	private final GraphCanvas graph;
	private int x, y, width, height, textHeight;

	private String[] keys;
	private Color[] colors;
	private Rectangle[] bounds;

	private static final int BORDER = 5;
	private static final int BOX_SIZE = 15;
	private static final String TITLE = Localization.getString("GraphLegend.Legend"); //$NON-NLS-1$
}
