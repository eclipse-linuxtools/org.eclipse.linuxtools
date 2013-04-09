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

import org.eclipse.linuxtools.dataviewers.piechart.PieChart;
import org.eclipse.linuxtools.systemtap.graphingapi.core.adapters.IAdapter;
import org.eclipse.swt.widgets.Composite;

/**
 * Builds Pie chart.
 */
public class PieChartBuilder extends AbstractChartWithoutAxisBuilder {
	public static final String ID = "org.eclipse.linuxtools.systemtap.graphingapi.ui.charts.piechartbuilder"; //$NON-NLS-1$

	public PieChartBuilder(Composite parent, int style, String title,IAdapter adapter) {
		super(adapter, parent, style, title);
	}

	@Override
	protected void createChart() {
		this.chart = new PieChart(this, getStyle());
	}

	@Override
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

	@Override
	public void updateDataSet() {
		buildXSeries();
	}
}
