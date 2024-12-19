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

import org.eclipse.linuxtools.internal.systemtap.graphing.ui.charts.BarChart;
import org.eclipse.linuxtools.internal.systemtap.graphing.ui.charts.Messages;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swtchart.IAxis;
import org.eclipse.swtchart.ISeries;

/**
 * This is a specialized mouse listener for displaying the value of a category (bar)
 * when the user hovers over it with the mouse. The value is displayed in a
 * tooltip popup near the mouse.
 * @author aferrazz
 * @since 3.0
 */
public class BarChartMouseMoveListener extends ToolTipChartMouseMoveListener {
	public BarChartMouseMoveListener(BarChart chart, Control parent) {
        super(chart, parent);
    }

    @Override
    public void mouseMove(MouseEvent e) {
        super.mouseMove(e);
		ISeries<?>[] allSeries = chart.getSeriesSet().getSeries();
        if (allSeries.length == 0) {
            return;
        }
        IAxis xAxis = chart.getAxisSet().getXAxis(0);
        String[] categorySeries = ((BarChart) chart).getCategorySeries();
        int barIndex = (int) xAxis.getDataCoordinate(e.x);
        if (0 <= barIndex && barIndex < categorySeries.length) {
            String textTip = ""; //$NON-NLS-1$
            for (int i = 0; i < allSeries.length; i++) {
                textTip = textTip.concat((i > 0 ? "\n" : "") + MessageFormat.format(Messages.BarChartBuilder_ToolTipCoords, //$NON-NLS-1$ //$NON-NLS-2$
                        allSeries[i].getId(), ((BarChart) chart).getBarValue(i, barIndex)));
            }
            setTextTip(textTip);
        } else {
            tipShell.setVisible(false);
        }
        chart.redraw();
    }
}