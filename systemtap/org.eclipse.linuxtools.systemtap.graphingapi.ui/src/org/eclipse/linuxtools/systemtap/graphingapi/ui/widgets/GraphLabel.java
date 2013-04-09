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

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

/**
 * The GraphLabel primitive is used to draw a string of text at an arbitrary point on the graph.
 * @author Ryan Morse
 *
 */
public class GraphLabel implements IGraphPrimitive {
	public GraphLabel(GraphCanvas canvas, String title, Point center, float size, int style) {
		this.canvas = canvas;
		this.title = title;
		this.center = center;
		this.size = size;
		this.style = style;
		this.parent = null;
	}

	public GraphLabel(GraphCanvas canvas, String title, Point center, int size, int style) {
		this(canvas, title, center, (float)size, style);
	}

	public GraphLabel(GraphCanvas canvas, String title, Composite parent, float size, int style) {
		this(canvas, title, parent.getSize(), size, style);
		this.parent = parent;
	}

	public GraphLabel(GraphCanvas canvas, String title, Composite parent, int size, int style) {
		this(canvas, title, parent, (float)size, style);
	}

	public int calculateSize(GC gc) {
		int width = 0;
		for (int i=0; i<title.length(); i++)
			width += gc.getCharWidth(title.charAt(i));
		return width;
	}

	@Override
	public void calculateBounds() {
		if(null != parent) {
			Point size = parent.getSize();
			x = (size.x - width)>>1;
			y = parent.getBounds().y;
		} else {
			x = center.x - (width>>1);
			y = center.y - (height>>1);
		}
	}

	@Override
	public boolean isVisible() {
		return true;
	}

	@Override
	public boolean isUnder(Point loc) {
		if(loc.x >= x && loc.y >= y && loc.x <= x+width && loc.y <=y+height)
			return true;
		return false;
	}

	@Override
	public void paint(GC gc) {
		if(staticSize)
			gc.setFont(new Font(canvas.getDisplay(), "Times", (int)size, style)); //$NON-NLS-1$
		else
			gc.setFont(new Font(canvas.getDisplay(), "Times", (int)(canvas.getSize().y * size), style)); //$NON-NLS-1$

		width = calculateSize(gc);
		height = gc.getFontMetrics().getHeight();
		calculateBounds();

		Color temp = gc.getForeground();
		gc.setForeground(canvas.axisColor);
		gc.drawText(title, x, y);

		gc.setForeground(temp);
	}

	private GraphCanvas canvas;
	private String title;
	private int width;
	private int height;
	private Point center;
	private Composite parent;

	private int x, y;
	private float size;
	private int style;
	private boolean staticSize;
}
