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

import org.eclipse.linuxtools.dataviewers.piechart.PieChart;
import org.eclipse.linuxtools.internal.systemtap.graphing.ui.charts.Messages;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.swtchart.ISeries;

/**
 * @since 3.0
 */
public class PieChartMouseMoveListener extends ToolTipChartMouseMoveListener {
    public PieChartMouseMoveListener(PieChart chart, Composite parent) {
        super(chart, parent);
    }

    @Override
    public void mouseMove(MouseEvent e) {
        super.mouseMove(e);
        ISeries[] allSeries = chart.getSeriesSet().getSeries();
        int numPies = allSeries.length > 0 ? allSeries[0].getXSeries().length : 0;
        int pieIndex = 0, sliceIndex = -1;
        for (; pieIndex < numPies; pieIndex++) {
            sliceIndex = ((PieChart) chart).getSliceIndexFromPosition(pieIndex, e.x, e.y);
            if (sliceIndex != -1) {
                break;
            }
        }
        if (sliceIndex != -1) {
            setTextTip(MessageFormat.format(Messages.PieChartBuilder_ToolTipCoords,
                    chart.getAxisSet().getXAxis(0).getTitle().getText(),
                    allSeries[sliceIndex].getId(), allSeries[sliceIndex].getXSeries()[pieIndex],
                    Math.round(((PieChart) chart).getSlicePercent(pieIndex, sliceIndex) * 100.0) / 100.0));
        } else {
            tipShell.setVisible(false);
        }
    }
}