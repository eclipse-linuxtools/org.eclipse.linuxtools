/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.linuxtools.valgrind.massif.gef;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.linuxtools.valgrind.massif.MassifPlugin;
import org.eclipse.linuxtools.valgrind.massif.MassifSnapshot;
import org.eclipse.linuxtools.valgrind.massif.MassifToolPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.widgets.Display;

public class MassifHeapChart extends Figure {
	protected MassifSnapshot[] snapshots;

	public MassifHeapChart(MassifSnapshot[] snapshots) {
		this.snapshots = snapshots;
	}

	@Override
	protected void paintFigure(Graphics graphics) {
		int[] xValues = new int[snapshots.length];
		int[] yValues = new int[snapshots.length];
		for (int i = 0; i < yValues.length; i++) {
			xValues[i] = snapshots[i].getTime();
			yValues[i] = snapshots[i].getTotal();
		}
		
		int maxXValue = snapshots[snapshots.length - 1].getTime();
		int maxYValue = findMax(yValues);

		TextLayout maxX = new TextLayout(Display.getCurrent());
		maxX.setAlignment(SWT.CENTER);
		maxX.setText(String.valueOf(maxXValue));

		TextLayout maxY = new TextLayout(Display.getCurrent());
		maxY.setAlignment(SWT.CENTER);
		maxY.setText(String.valueOf(maxYValue));
		Rectangle clientArea = getClientArea();
		Rectangle plotArea = clientArea.getCopy();

		// shrink by a uniform amount equal to the max value label's width plus some padding
		int padding = maxY.getBounds().width + 5;
		plotArea.shrink(padding, padding);

		if (yValues.length > 0) {
			// plot data
			PointList points = new PointList(yValues.length);
			for (int i = 0; i < yValues.length; i++) {
				points.addPoint(new Point(xValues[i] * plotArea.width / maxXValue, plotArea.height * (maxYValue - yValues[i]) / maxYValue));
			}
			
			points.performTranslate(plotArea.x, plotArea.y);
			Color old = graphics.getForegroundColor();
			graphics.setForegroundColor(ColorConstants.blue);
			graphics.drawPolyline(points);
			graphics.setForegroundColor(old);
		}
		//		for (int i = 0; i < values.length; i++) {
		//			Rectangle bar = plotArea.getCopy();
		//			bar.width /= values.length;
		//			bar.x += i * bar.width;
		//			bar.shrink(1, 1);
		//			int chop = bar.height * (maxValue - values[i]) / maxValue;
		//			bar.crop(new Insets(chop, 0, 0, 0));
		//			graphics.setBackgroundColor(ColorConstants.red);
		//			graphics.fillRectangle(bar);
		//		}

		Point origin = new Point(plotArea.x, plotArea.y + plotArea.height);
		Point xEnd = new Point(plotArea.x + plotArea.width + 5, plotArea.y + plotArea.height);
		Point yEnd = new Point(plotArea.x, plotArea.y - 10);
		graphics.drawLine(origin, xEnd); // x axis
		graphics.drawLine(origin, yEnd); // y axis
		graphics.drawLine(xEnd, xEnd.getTranslated(-4, -4)); // x axis arrow
		graphics.drawLine(xEnd, xEnd.getTranslated(-4, 4)); // x axis arrow
		graphics.drawLine(yEnd, yEnd.getTranslated(-4, 4)); // y axis arrow
		graphics.drawLine(yEnd, yEnd.getTranslated(4, 4)); // y axis arrow

		graphics.drawTextLayout(maxX, xEnd.x - maxX.getBounds().width - 5, xEnd.y + 5);
		graphics.drawTextLayout(maxY, 5, yEnd.y + 5);
		// x units
		TextLayout xUnits = new TextLayout(Display.getCurrent());
		xUnits.setAlignment(SWT.CENTER);
		try {
			xUnits.setText(MassifPlugin.getDefault().getConfig().getAttribute(MassifToolPage.ATTR_MASSIF_TIMEUNIT, "i")); //$NON-NLS-1$
			graphics.drawTextLayout(xUnits, xEnd.x + 2, xEnd.y - xUnits.getBounds().height / 2);
		} catch (CoreException e) {
			e.printStackTrace();
		}

		// y units
		TextLayout yUnits = new TextLayout(Display.getCurrent());
		yUnits.setAlignment(SWT.CENTER);
		yUnits.setText("B"); //$NON-NLS-1$

		graphics.drawTextLayout(yUnits, yEnd.x, yEnd.y - yUnits.getBounds().height);
	}

	private int findMax(int[] values) {
		int max = 0;
		for (int i = 0; i < values.length; i++) {
			if (values[i] > max) {
				max = values[i];
			}
		}
		return max;
	}
}
