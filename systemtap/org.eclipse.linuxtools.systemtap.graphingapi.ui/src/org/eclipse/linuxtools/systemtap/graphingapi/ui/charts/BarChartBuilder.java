/****************************************************************
 * Copyright (c) 2006-2013 IBM Corp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM - initial API and implementation
 *
 ****************************************************************
 */
package org.eclipse.linuxtools.systemtap.graphingapi.ui.charts;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.linuxtools.systemtap.graphingapi.core.adapters.IAdapter;

import org.swtchart.IBarSeries;
import org.swtchart.ISeries;
import org.swtchart.ISeries.SeriesType;

/**
 * Builds bar chart.
 * 
 * @author Qi Liang
 */

public class BarChartBuilder extends AbstractChartWithAxisBuilder {
	public static final String ID = "org.eclipse.linuxtools.systemtap.graphingapi.ui.charts.barchartbuilder"; //$NON-NLS-1$

    public BarChartBuilder(Composite parent, int style, String title,IAdapter adapter) {
		super(adapter, parent, style, title);
    }
    
	@Override
	protected ISeries createChartISeries(int i) {
		IBarSeries series = (IBarSeries)chart.getSeriesSet().
			createSeries(SeriesType.BAR, adapter.getLabels()[i+1]);
		series.setBarColor(COLORS[i % COLORS.length]);
		return series;
	}

	@Override
	public void updateDataSet() {
		buildXSeries();
	}
}
