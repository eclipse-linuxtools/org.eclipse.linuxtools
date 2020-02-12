/*******************************************************************************
 * Copyright (c) 2014, 2018 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - Initial API and implementation.
 *******************************************************************************/
package org.eclipse.linuxtools.internal.systemtap.graphing.ui.charts.listeners;

import java.text.MessageFormat;

import org.eclipse.linuxtools.internal.systemtap.graphing.ui.charts.Messages;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swtchart.Chart;
import org.eclipse.swtchart.ISeries;

/**
 * This is a specialized mouse listener for displaying the coordinates of a data point on
 * the chart that the user hovers over with the mouse. The coordinates are displayed in a
 * tooltip popup near the mouse.
 * @since 3.0
 */
public class ChartWithAxisMouseMoveListener extends ToolTipChartMouseMoveListener {
    private static final double DIST_TOLERANCE = 20.0;

	public ChartWithAxisMouseMoveListener(Chart chart, Control hoverArea) {
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