/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial API and implementation.
 *******************************************************************************/
package org.eclipse.linuxtools.internal.systemtap.graphing.ui.charts.listeners;

import java.text.MessageFormat;

import org.eclipse.linuxtools.internal.systemtap.graphing.ui.charts.Messages;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.swtchart.Chart;
import org.swtchart.ISeries;

/**
 * This is a specialized mouse listener for displaying the coordinates of a data point on
 * the chart that the user hovers over with the mouse. The coordinates are displayed in a
 * tooltip popup near the mouse.
 * @since 3.0
 */
public class ChartWithAxisMouseMoveListener extends ToolTipChartMouseMoveListener {
    private static final double DIST_TOLERANCE = 20.0;

    public ChartWithAxisMouseMoveListener(Chart chart, Composite hoverArea) {
        super(chart, hoverArea);
    }

    @Override
    public void mouseMove(MouseEvent e) {
        super.mouseMove(e);
        double closestDistance = DIST_TOLERANCE;
        int closestIndex = -1;
        ISeries closestSeries = null;
        Point closestPoint = null;
        for (ISeries series : chart.getSeriesSet().getSeries()) {
            for (int i = 0; i < series.getXSeries().length; i++) {
                Point dataPoint = series.getPixelCoordinates(i);
                if (dataPoint.x >= 0 && dataPoint.y >= 0) {
                    double dist = Math.sqrt(Math.pow(dataPoint.x - e.x, 2) + Math.pow(dataPoint.y - e.y, 2));
                    if (dist < closestDistance) {
                        closestDistance = dist;
                        closestIndex = i;
                        closestSeries = series;
                        closestPoint = dataPoint;
                    }
                }
            }
        }
        if (closestPoint != null) {
            setTextTip(MessageFormat.format(Messages.AbstractChartWithAxisBuilder_ToolTipCoords,
                    closestSeries.getId(), closestSeries.getXSeries()[closestIndex], closestSeries.getYSeries()[closestIndex]));
        } else {
            tipShell.setVisible(false);
        }
    }
}