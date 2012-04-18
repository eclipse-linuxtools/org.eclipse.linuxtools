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

import org.swtchart.ILineSeries;
import org.swtchart.ISeries;
import org.swtchart.ISeries.SeriesType;
import org.swtchart.LineStyle;

/**
 * Builds bar chart.
 * 
 * @author Qi Liang
 */

public class LineChartBuilder extends AbstractChartWithAxisBuilder {

	public static final String ID = "org.eclipse.linuxtools.systemtap.ui.graphingapi.ui.charts.linechartbuilder";

    public LineChartBuilder(Composite parent, int style, String title,IAdapter adapter) {
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

	public void updateDataSet() {
		buildXSeries();
	}

	protected ISeries createChartISeries(int i) {
		ILineSeries series = (ILineSeries) chart.getSeriesSet().
			createSeries(SeriesType.LINE, adapter.getLabels()[i+1]);
		series.setSymbolColor(COLORS[i % COLORS.length]);
		series.setLineColor(COLORS[i % COLORS.length]);
		series.setLineStyle(LineStyle.SOLID);
		return series;
	}
}
