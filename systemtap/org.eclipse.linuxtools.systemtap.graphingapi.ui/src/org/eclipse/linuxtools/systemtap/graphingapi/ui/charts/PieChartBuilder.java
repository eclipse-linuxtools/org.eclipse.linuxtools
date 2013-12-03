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
import org.swtchart.IAxis;
import org.swtchart.Range;

/**
 * Builds Pie chart.
 */
public class PieChartBuilder extends AbstractChartWithoutAxisBuilder {
	public static final String ID = "org.eclipse.linuxtools.systemtap.graphingapi.ui.charts.piechartbuilder"; //$NON-NLS-1$

	public PieChartBuilder(Composite parent, int style, String title, IAdapter adapter) {
		super(adapter, parent, style, title);
	}

	@Override
	protected void createChart() {
		this.chart = new PieChart(this, getStyle());
		((PieChart) chart).setCustomColors(COLORS);
		chartMouseMoveListener = new PieChartMouseMoveListener((PieChart) chart, chart);
	}

	@Override
	protected void buildXAxis() {
		String[] labels = adapter.getLabels();
		String[] seriesLabels = new String[labels.length - 1];
		for (int i = 0; i < seriesLabels.length; i++) {
			seriesLabels[i] = labels[i+1];
		}
		IAxis xAxis = this.chart.getAxisSet().getXAxis(0);
		xAxis.getTitle().setText(labels[0]);
		xAxis.setCategorySeries(seriesLabels);
	}

	@Override
	protected void buildXSeries() {
		Object data[][] = adapter.getData();
		if (data == null || data.length == 0) {
			return;
		}

		int start = 0, len = Math.min(this.maxItems, data.length), leny = data[0].length-1;
		if (this.maxItems < data.length) {
			start = data.length - this.maxItems;
		}

		Double[][] all_values = new Double[len][leny];
		String[] all_labels = new String[len];

		for (int i = 0; i < all_labels.length; i++) {
			Object label = data[start + i][0];
			if (label != null) {
				all_labels[i] = label.toString();
				for (int j = 1; j < data[start + i].length; j++) {
					Double val = getDoubleOrNullValue(data[start + i][j]);
					if (val != null) {
						all_values[i][j-1] = val;
					} else {
						all_values[i][j-1] = 0.0;
					}
				}
			}
		}

		double[][] values = new double[len][leny];
		String[] labels = new String[len];
		int len_trim = 0;
		for (int i = 0; i < len; i++) {
			if (all_labels[i] != null) {
				labels[len_trim] = all_labels[i];
				for (int j = 0; j < leny; j++) {
					values[len_trim][j] = all_values[i][j].doubleValue();
				}
				len_trim++;
			}
		}
		double[][] values_trim = new double[len_trim][leny];
		String[] labels_trim = new String[len_trim];
		for (int i = 0; i < len_trim; i++) {
			labels_trim[i] = labels[i];
			for (int j = 0; j < leny; j++) {
				values_trim[i][j] = values[i][j];
			}
		}

		((PieChart)this.chart).addPieChartSeries(getUniqueNames(labels_trim), values_trim);
		applyCategoryRange(values_trim[0].length);
		chart.redraw();
	}

	/**
	 * This updates the visible range of the chart's x-axis.
	 */
	private void applyCategoryRange(int numItems) {
		int itemRange = Math.max(1, (int) Math.ceil(numItems * scale)); // The number of items to display
		int lower = (int) Math.round((numItems - itemRange) * scroll);
		chart.getAxisSet().getXAxis(0).setRange(new Range(lower, lower + itemRange - 1));
	}

	@Override
	public void updateDataSet() {
		buildXSeries();
		chartMouseMoveListener.update();
	}
}
