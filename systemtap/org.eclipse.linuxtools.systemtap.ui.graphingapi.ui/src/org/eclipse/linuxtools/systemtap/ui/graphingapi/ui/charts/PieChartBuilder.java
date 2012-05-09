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

import org.eclipse.linuxtools.dataviewers.piechart.PieChart;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.adapters.IAdapter;

/**
 * Builds Pie chart.
 */
public class PieChartBuilder extends AbstractChartWithoutAxisBuilder {
	public static final String ID = "org.eclipse.linuxtools.systemtap.ui.graphingapi.ui.charts.piechartbuilder";

	public PieChartBuilder(Composite parent, int style, String title,IAdapter adapter) {
		super(adapter, parent, style, title);
	}

	protected void createChart() {
		this.chart = new PieChart(this, getStyle());
	}

	protected void buildXSeries() {
		Object data[][] = adapter.getData();
		if (data == null || data.length == 0 || data[0].length == 0)
			return;

		int start = 0, len = Math.min(this.maxItems, data.length);
		if (this.maxItems < data.length) {
			start = data.length - this.maxItems;
		}

		double[][] values = new double[len][data[0].length-1];
		String[] labels = new String[len];

		for (int i = 0; i < labels.length; i++) {
			if (data[i].length < 2)
				return;
			labels[i] = data[start + i][0].toString();
			for (int j = 1; j < data[start + i].length; j++)
				values[i][j-1] = getDoubleValue(data[start + i][j]);
		}

		((PieChart)this.chart).addPieChartSeries(labels, values);
		chart.redraw();
	}

	public void updateDataSet() {
		buildXSeries();
	}
}
