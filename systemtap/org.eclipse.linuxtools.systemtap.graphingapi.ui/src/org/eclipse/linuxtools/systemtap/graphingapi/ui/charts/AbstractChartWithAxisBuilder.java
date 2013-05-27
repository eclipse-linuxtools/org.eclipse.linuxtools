/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.graphingapi.ui.charts;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.linuxtools.internal.systemtap.graphingapi.ui.GraphingAPIUIPlugin;
import org.eclipse.linuxtools.systemtap.graphingapi.core.adapters.IAdapter;
import org.eclipse.linuxtools.systemtap.graphingapi.ui.preferences.GraphingAPIPreferenceConstants;
import org.eclipse.swt.widgets.Composite;
import org.swtchart.IAxis;
import org.swtchart.ISeries;
import org.swtchart.ITitle;
import org.swtchart.LineStyle;

/**
 * Builds the chart with axis.
 *
 * @author Qi Liang
 */
public abstract class AbstractChartWithAxisBuilder extends AbstractChartBuilder {

    /**
     * Title of X axis.
     */
    protected String xTitle = null;
	protected boolean xLineGrid, yLineGrid;

	/**
	 * Create a chart series for that chart.
	 */
	protected abstract ISeries createChartISeries(int i);

    /**
     * Constructor.
     */

    public AbstractChartWithAxisBuilder(IAdapter adapter, Composite parent, int style, String title) {
    	 super(adapter, parent, style, title);
		IPreferenceStore store = GraphingAPIUIPlugin.getDefault().getPreferenceStore();
		xLineGrid = store.getBoolean(GraphingAPIPreferenceConstants.P_SHOW_X_GRID_LINES);
		yLineGrid = store.getBoolean(GraphingAPIPreferenceConstants.P_SHOW_Y_GRID_LINES);
	}

	/**
	 * Builds X axis.
	 */
	@Override
	protected void buildXAxis() {
		String labels[] = adapter.getLabels();
		IAxis xAxis = this.chart.getAxisSet().getXAxis(0);
		if (xLineGrid)
			xAxis.getGrid().setStyle(LineStyle.SOLID);
		else
			xAxis.getGrid().setStyle(LineStyle.NONE);
		xAxis.getTick().setForeground(BLACK);
		ITitle xTitle = xAxis.getTitle();
		xTitle.setForeground(BLACK);

		if (labels.length > 0)
			xTitle.setText(labels[0]);
		else
			xTitle.setText(""); //$NON-NLS-1$
	}

	/**
	 * Builds Y axis.
	 */
	@Override
	protected void buildYAxis() {
		IAxis yAxis = this.chart.getAxisSet().getYAxis(0);
		yAxis.getTitle().setText(""); //$NON-NLS-1$
		if (yLineGrid)
			yAxis.getGrid().setStyle(LineStyle.SOLID);
		else
			yAxis.getGrid().setStyle(LineStyle.NONE);
		yAxis.getTick().setForeground(BLACK);
	}

	/**
	 * Builds X series.
	 */
	@Override
	protected void buildXSeries() {
		Object data[][] = adapter.getData();
		if (data == null || data.length == 0)
			return;

		int totalMaxItems = (int)Math.round(this.maxItems * scale);
		int start = 0, len = Math.min(totalMaxItems, data.length);
		if (totalMaxItems < data.length) {
			start = data.length - totalMaxItems;
		}

		double[] valx = new double[len];
		double[][] valy = new double[data[0].length-1][len];

		ISeries allSeries[] = chart.getSeriesSet().getSeries();
		for (int i = 0; i < valx.length; i++)
			for (int j = 0; j < data[start + i].length; j++) {
				if (j == 0)
					valx[i] = getDoubleValue(data[start + i][j]);
				else
					valy[j-1][i] = getDoubleValue(data[start + i][j]);
			}

		for (int i = 0; i < valy.length; i++) {
			ISeries series;
			if (i >= allSeries.length) {
				series = createChartISeries(i);
			} else {
				series = chart.getSeriesSet().getSeries()[i];
			}
			series.setXSeries(valx);
			series.setYSeries(valy[i]);
		}

		chart.getAxisSet().adjustRange();
		chart.redraw();
	}
}
