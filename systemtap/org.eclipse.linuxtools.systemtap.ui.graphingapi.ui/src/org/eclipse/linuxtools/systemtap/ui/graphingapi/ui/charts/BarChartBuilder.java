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

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import org.eclipse.linuxtools.internal.systemtap.ui.graphingapi.ui.GraphingAPIUIPlugin;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.adapters.BlockAdapter;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.adapters.IAdapter;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.ui.preferences.GraphingAPIPreferenceConstants;

import org.swtchart.IAxis;
import org.swtchart.IBarSeries;
import org.swtchart.ISeries;
import org.swtchart.ISeries.SeriesType;
import org.swtchart.Range;

/**
 * Builds bar chart.
 * 
 * @author Qi Liang
 */

public class BarChartBuilder extends AbstractChartWithAxisBuilder {

    protected AbstractChartBuilder builder = null;

	private boolean fullUpdate;
	protected int xSeriesTicks;
	protected static int ySeriesTicks;
	protected static int maxItems;
	protected static int viewableItems;

	private static final Color[] BAR_COLORS = {
												new Color(Display.getDefault(), 255, 0, 0),
												new Color(Display.getDefault(), 0, 255, 0),
												new Color(Display.getDefault(), 0, 0, 255),
												new Color(Display.getDefault(), 255, 255, 0),
												new Color(Display.getDefault(), 255, 0, 255),
												new Color(Display.getDefault(), 0, 255, 255),
												new Color(Display.getDefault(), 0, 0, 0),
												new Color(Display.getDefault(), 64, 128, 128),
												new Color(Display.getDefault(), 255, 165, 0),
												new Color(Display.getDefault(), 128, 128, 128),
												};
	public static final String ID = "org.eclipse.linuxtools.systemtap.ui.graphingapi.ui.charts.barchartbuilder";

    public BarChartBuilder(Composite parent, int style, String title,IAdapter adapter) {
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

	/**
	 * Builds X series.
	 */
	protected void buildXSeries() {
		Object data[][] = adapter.getData();
		if (data == null || data.length == 0)
			return;

		double[] valx = new double[data.length];
		double[][] valy = new double[data[0].length-1][data.length];


		for (int i = 0; i < data.length; i++)
			for (int j = 0; j < data[i].length; j++) {
				if (j == 0)
					valx[i] = getDoubleValue(data[i][j]);
				else
					valy[j-1][i] = getDoubleValue(data[i][j]);
			}

		ISeries allSeries[] = chart.getSeriesSet().getSeries();
		for (int i = 0; i < valy.length; i++) {
			ISeries series;
			if (i >= allSeries.length) {
				series = chart.getSeriesSet().
					createSeries(SeriesType.BAR, adapter.getLabels()[i+1]);
				((IBarSeries)series).setBarColor(BAR_COLORS[i % BAR_COLORS.length]);
			} else {
				series = chart.getSeriesSet().getSeries()[i];
			}
			series.setXSeries(valx);
			series.setYSeries(valy[i]);
		}

		chart.getAxisSet().adjustRange();
		chart.redraw();
	}

	public void updateDataSet() {
		buildXSeries();
	}
}
