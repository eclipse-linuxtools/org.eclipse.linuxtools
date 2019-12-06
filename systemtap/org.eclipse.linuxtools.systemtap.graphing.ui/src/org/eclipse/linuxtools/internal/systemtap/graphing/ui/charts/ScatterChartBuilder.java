/****************************************************************
 * Copyright (c) 2006, 2018 IBM Corp. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM - initial API and implementation
 *
 ****************************************************************/
package org.eclipse.linuxtools.internal.systemtap.graphing.ui.charts;

import org.eclipse.linuxtools.systemtap.graphing.core.adapters.IAdapter;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swtchart.ILineSeries;
import org.eclipse.swtchart.ISeries;
import org.eclipse.swtchart.LineStyle;

/**
 * Builds bar chart.
 */

public class ScatterChartBuilder extends LineChartBuilder {
    public static final String ID = "org.eclipse.linuxtools.systemtap.graphing.ui.charts.scatterchartbuilder"; //$NON-NLS-1$

    public ScatterChartBuilder(Composite parent, int style, String title,IAdapter adapter) {
        super(parent, style, title, adapter);
    }

    @Override
    protected ISeries createChartISeries(int i) {
        ILineSeries series = (ILineSeries)super.createChartISeries(i);
        series.setSymbolColor(COLORS[i % COLORS.length]);
        series.setLineStyle(LineStyle.NONE);
        return series;
    }
}
