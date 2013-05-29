/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Renato Stoffalette Joao <rsjoao@br.ibm.com>
 *******************************************************************************/
package org.eclipse.linuxtools.dataviewers.piechart;

import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.swtchart.Chart;
import org.swtchart.IBarSeries;
import org.swtchart.ISeries;

public class PieChart extends Chart {

    public PieChart(Composite parent, int style) {
        super(parent, style);
        Control plotArea = null;
        for (Control child : getChildren()) {
            if (child.getClass().getName().equals("org.swtchart.internal.axis.AxisTitle")) //$NON-NLS-1$
                child.setVisible(false); // Don't show original Plot Area and axis
            else if (child.getClass().getName().equals("org.swtchart.internal.PlotArea")) { //$NON-NLS-1$
                child.setVisible(false); // Don't show original Plot Area and axis
                plotArea = child;
            }
        }
        this.addPaintListener(new PieChartPaintListener(this, plotArea));
    }

    @Override
    public void addPaintListener(PaintListener listener) {
        if (!listener.getClass().getName().startsWith("org.swtchart.internal.axis")) //$NON-NLS-1$
            super.addPaintListener(listener);
    }

    /*
     * Add data to this Pie Chart. A single pie Chart will be drawn with the data provided.
     */
    public void addPieChartSeries(String labels[], double val[]) {
        for (ISeries s : this.getSeriesSet().getSeries())
            this.getSeriesSet().deleteSeries(s.getId());
        double newVal[][] = new double[val.length][1];
        for (int i = 0; i < val.length; i++)
            newVal[i][0] = val[i];
        addPieChartSeries(labels, newVal);
    }

    /*
     * Add data to this Pie Chart. We'll build one pie chart for each value in the array provided. The val matrix must
     * have an array of an array of values. Ex. labels = {'a', 'b'} val = {{1,2,3}, {4,5,6}} This will create 3 pie
     * charts. For the first one, 'a' will be 1 and 'b' will be 4. For the second chart 'a' will be 2 and 'b' will be 5.
     * For the third 'a' will be 3 and 'b' will be 6.
     */
    public void addPieChartSeries(String labels[], double val[][]) {
        for (ISeries s : this.getSeriesSet().getSeries())
            this.getSeriesSet().deleteSeries(s.getId());

        int size = Math.min(labels.length, val.length);
        for (int i = 0; i < size; i++) {
            IBarSeries s = (IBarSeries) this.getSeriesSet().createSeries(ISeries.SeriesType.BAR, labels[i]);
            double d[] = new double[val[i].length];
            for (int j = 0; j < val[i].length; j++)
                d[j] = val[i][j];
            s.setXSeries(d);
            s.setBarColor(new Color(this.getDisplay(), IColorsConstants.COLORS[i]));
        }
    }
}
