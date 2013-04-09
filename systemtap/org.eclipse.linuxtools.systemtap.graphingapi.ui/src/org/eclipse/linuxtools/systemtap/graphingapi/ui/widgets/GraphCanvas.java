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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;

/**
 * The Graph Canvas class provides a Canvas type object that renders IGraphPrimitive objects on itself.
 * It is an implementation of the IWidgetContainer interface and can be used by Graph Primitives to scale
 * themselves.
 *
 * This class is the parent class of the entire Graph system, all of the decendents from Graph are
 * based upon this class.
 * @author Henry Hughes
 * @author Ryan Morse
 * @see org.eclipse.linuxtools.systemtap.graphingapi.ui.graphs.Graph
 *
 */
public class GraphCanvas extends Canvas {
	/**
	 * The default constructor for GraphCanvas. Creates a canvas of the appropriate size, then
	 * sets the internal area rectangles, the internal padding and scaling levels, enables scrolling,
	 * and sets the default axis color.
	 * @param parent Parent composite for this canvas.
	 * @param style SWT Style flags for this canvas (use SWT.NONE)
	 */
	public GraphCanvas(Composite parent, int style) {
		super(parent, style | SWT.V_SCROLL | SWT.H_SCROLL);

		globalArea = new Rectangle(0, 0, 0, 0);
		localArea = new Rectangle(0, 0, 0, 0);

		xpad = 10;
		ypad = 10;
		scale = 1.0;
		autoScroll = true;

		axisColor = parent.getDisplay().getSystemColor(SWT.COLOR_GRAY);

		hBar = this.getHorizontalBar();
		vBar = this.getVerticalBar();
		hBar.addListener (SWT.Selection, new Listener () {
			@Override
			public void handleEvent (Event e) {
				if(hBar.getSelection()+hBar.getThumb() == hBar.getMaximum())
					autoScroll = true;
				else
					autoScroll = false;

				setLocalArea(new Rectangle(hBar.getSelection(), localArea.y, localArea.width, localArea.height), true);
				redraw();
			}
		});

		vBar.addListener (SWT.Selection, new Listener () {
			@Override
			public void handleEvent (Event e) {
				autoScroll = false;

				setLocalArea(new Rectangle(localArea.x, vBar.getMaximum()-(vBar.getSelection()+vBar.getThumb())+vBar.getMinimum(), localArea.width, localArea.height), true);
				redraw();
			}
		});

	}
	/**
	 * An accessor to set the X padding width. Padding is defined as pixels inside the bounds
	 * of the drawable area that are left blank when rendering the graph. Valid values are integers
	 * greater than 0. The canvas will silently ignore requests to set the padding width to <=0.
	 * @param xpad New X padding value.
	 */
	public void setXPadding(int xpad) { this.xpad = xpad > 0 ? xpad : this.xpad; }
	/**
	 * An accessor to set the Y padding width. Padding is defined as pixels inside the bounds
	 * of the drawable area that are left blank when rendering the graph. Valid values are integers
	 * greater than 0. The canvas will silently ignore requests to set the padding width to <=0.
	 * @param xpad New Y padding value.
	 */
	public void setYPadding(int ypad) { this.ypad = ypad > 0 ? ypad : this.ypad; }
	/**
	 * Returns the current X padding value.
	 * @return The X padding width value, in pixels.
	 */
	public int getXPadding() { return xpad; }
	/**
	 * Returns the current Y padding value.
	 * @return The Y padding width value, in pixels.
	 */
	public int getYPadding() { return ypad; }

	/**
	 * The repaint method is called when the graph is out of date and needs to be redrawn. This is an
	 * abstraction method around <code>Canvas.redraw</code> that synchronously executes the request
	 * on the display thread, blocking the calling thread until the repaint is completed.
	 */
	public synchronized void repaint() {
		getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				redraw();
			}
		});
	}

	/**
	 * Returns the size of the graphing area of the canvas.
	 */
	@Override
	public Point getSize() {
		Point p = new Point(super.getSize().x, super.getSize().y);
		p.x -= vBar.getSize().x+5;
		p.y -= hBar.getSize().y+5;
		return p;
	}

	/**
	 * Resets the canvas size to the specified area.
	 */
	public void setGlobalArea(Rectangle area) {
		globalArea = area;

		hBar.setMinimum(getGlobalXMin());
		hBar.setMaximum (getGlobalXMax());
		vBar.setMinimum(getGlobalYMin());
		vBar.setMaximum (getGlobalYMax());
	}

	/**
	 * Sets the size of the graphing area to the specified area.
	 */
	public void setLocalArea(Rectangle area) {
		setLocalArea(area, false);
	}

	public void setLocalArea(Rectangle area, boolean force) {
		if(autoScroll || force) {
			localArea = area;

			hBar.setThumb (getLocalWidth());
			vBar.setThumb (getLocalHeight());

			hBar.setIncrement(getLocalWidth()>>3);
			hBar.setPageIncrement(getLocalWidth());
			vBar.setIncrement(getLocalHeight()>>3);
			vBar.setPageIncrement(getLocalHeight());
		}
		if(autoScroll) {
			hBar.setSelection(hBar.getMaximum());
			vBar.setSelection(getGlobalYMax() - Math.min(getLocalYMax(), getGlobalYMax()) + getGlobalYMin());
		}
	}

	public int getGlobalXMin() {
		return globalArea.x;
	}

	public int getLocalXMin() {
		return localArea.x;
	}

	public int getGlobalXMax() {
		return globalArea.x+globalArea.width;
	}

	public int getLocalXMax() {
		return getLocalXMin() + getLocalWidth();
	}

	public int getGlobalYMin() {
		return globalArea.y;
	}

	public int getLocalYMin() {
		return localArea.y;
	}

	public int getGlobalYMax() {
		return globalArea.y+globalArea.height;
	}

	public int getLocalYMax() {
		return getLocalYMin() + getLocalHeight();
	}

	public int getLocalWidth() {
		return (int)(localArea.width / scale);
	}

	public int getLocalHeight() {
		return (int)(localArea.height / scale);
	}

	protected Color axisColor;
	private int xpad, ypad;
	private ScrollBar hBar, vBar;
	private Rectangle globalArea, localArea;
	private boolean autoScroll;
	private double scale;
}
