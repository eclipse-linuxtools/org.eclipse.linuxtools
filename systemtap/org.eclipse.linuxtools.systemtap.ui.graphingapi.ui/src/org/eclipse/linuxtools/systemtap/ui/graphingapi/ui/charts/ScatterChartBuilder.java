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

import org.eclipse.jface.preference.IPreferenceStore;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.linuxtools.internal.systemtap.ui.graphingapi.ui.GraphingAPIUIPlugin;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.adapters.BlockAdapter;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.adapters.IAdapter;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.ui.preferences.GraphingAPIPreferenceConstants;

import org.swtchart.ILineSeries;
import org.swtchart.ISeries;
import org.swtchart.ISeries.SeriesType;
import org.swtchart.LineStyle;

/**
 * Builds bar chart.
 * 
 * @author Qi Liang
 */

public class ScatterChartBuilder extends AbstractChartWithAxisBuilder {

    protected AbstractChartBuilder builder = null;

	private boolean fullUpdate;
	protected int xSeriesTicks;
	protected static int ySeriesTicks;
	protected static int maxItems;
	protected static int viewableItems;

	public static final String ID = "org.eclipse.linuxtools.systemtap.ui.graphingapi.ui.charts.scatterchartbuilder";

    public ScatterChartBuilder(Composite parent, int style, String title,IAdapter adapter) {
		super(adapter, parent, style, title);
		fullUpdate = (adapter instanceof BlockAdapter) ? true : false;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.ibm.examples.chart.widget.AbstractChartBuilder#buildChart()
     */
    protected void createChart() {
		super.createChart();
    	IPreferenceStore store = GraphingAPIUIPlugin.getDefault().getPreferenceStore();
		xSeriesTicks = store.getInt(GraphingAPIPreferenceConstants.P_X_SERIES_TICKS);
		ySeriesTicks = store.getInt(GraphingAPIPreferenceConstants.P_Y_SERIES_TICKS);
		maxItems = store.getInt(GraphingAPIPreferenceConstants.P_MAX_DATA_ITEMS);
		viewableItems = store.getInt(GraphingAPIPreferenceConstants.P_VIEWABLE_DATA_ITEMS);
    }

	public void setScale(double scale) {
		xSeriesTicks = (int) (((Integer)xSeriesTicks).doubleValue() * scale);
		handleUpdateEvent();
	}

	public void updateDataSet() {
		buildXSeries();
	}

	protected ISeries createChartISeries(int i) {
		ILineSeries series = (ILineSeries) chart.getSeriesSet().
			createSeries(SeriesType.LINE, adapter.getLabels()[i+1]);
		series.setSymbolColor(COLORS[i % COLORS.length]);
		series.setLineStyle(LineStyle.NONE);
		return series;
	}
}
