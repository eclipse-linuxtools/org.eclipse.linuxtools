/****************************************************************
 * Licensed Material - Property of IBM
 *
 * ****-*** 
 *
 * (c) Copyright IBM Corp. 2006.  All rights reserved.
 *
 * US Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with
 * IBM Corp.
 *
 ****************************************************************
 */
package org.eclipse.linuxtools.systemtap.ui.graphingapi.ui.charts;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.adapters.IAdapter;

import org.swtchart.IBarSeries;
import org.swtchart.ISeries;
import org.swtchart.ISeries.SeriesType;

/**
 * Builds bar chart.
 * 
 * @author Qi Liang
 */

public class BarChartBuilder extends AbstractChartWithAxisBuilder {
	public static final String ID = "org.eclipse.linuxtools.systemtap.ui.graphingapi.ui.charts.barchartbuilder";

    public BarChartBuilder(Composite parent, int style, String title,IAdapter adapter) {
		super(adapter, parent, style, title);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.ibm.examples.chart.widget.AbstractChartBuilder#buildChart()
     */
    protected void createChart() {
		super.createChart();
    }

	public void setScale(double scale) {
		handleUpdateEvent();
	}

	protected ISeries createChartISeries(int i) {
		IBarSeries series = (IBarSeries)chart.getSeriesSet().
			createSeries(SeriesType.BAR, adapter.getLabels()[i+1]);
		series.setBarColor(COLORS[i % COLORS.length]);
		return series;
	}

	public void updateDataSet() {
		buildXSeries();
	}
}
