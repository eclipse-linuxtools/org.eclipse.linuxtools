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

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.linuxtools.internal.systemtap.graphingapi.ui.GraphingAPIUIPlugin;
import org.eclipse.linuxtools.systemtap.graphingapi.core.adapters.IAdapter;
import org.eclipse.linuxtools.systemtap.graphingapi.ui.preferences.GraphingAPIPreferenceConstants;
import org.eclipse.linuxtools.systemtap.structures.listeners.IUpdateListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.swtchart.Chart;
import org.swtchart.ITitle;

/**
 * Provides the common members and the framework to build one chart.
 *
 * @author Qi Liang
 */
public abstract class AbstractChartBuilder extends Composite implements IUpdateListener{

	/**
	 * Font name for all titles, labels, and values.
	 */
	protected final static String FONT_NAME = "MS Sans Serif"; //$NON-NLS-1$
	protected int maxItems;
	protected double scale = 1.0;

	/**
	 * Provides data for chart.
	 */
	protected IAdapter adapter = null;
	protected int xseries;
	protected int[] yseries;

	protected static final Color WHITE = new Color(Display.getDefault(), 255, 255, 255);
	protected static final Color BLACK = new Color(Display.getDefault(), 0, 0, 0);
	protected static final Color RED = new Color(Display.getDefault(), 255, 0, 0);

	protected static final Color[] COLORS = {
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
	/**
	 * Chart instance.
	 */
	protected Chart chart = null;

	/**
	 * Chart title.
	 */
	protected String title = null;

	public abstract void updateDataSet();

	/**
	 * Constructs one chart builder and associate it to one data set.
	 *
	 * @param dataSet
	 *            data set
	 */

	public AbstractChartBuilder(IAdapter adapter, Composite parent, int style, String title) {
		super(parent, style);
		this.adapter = adapter;
		this.title = title;
		this.setLayout(new FillLayout());
		IPreferenceStore store = GraphingAPIUIPlugin.getDefault().getPreferenceStore();
		maxItems = Math.min(store.getInt(GraphingAPIPreferenceConstants.P_VIEWABLE_DATA_ITEMS),
									store.getInt(GraphingAPIPreferenceConstants.P_MAX_DATA_ITEMS));
	}

	/**
	 * Builds one chart.
	 */
	public void build() {
		createChart();
		buildPlot();
		buildLegend();
		buildTitle();
		buildXAxis();
		buildYAxis();
		buildXSeries();
		buildYSeries();
		updateDataSet();
	}

	/**
	 * Creates chart instance.
	 */
	protected void createChart() {
		this.chart = new Chart(this, getStyle());
	}

	/**
	 * Builds plot.
	 */
	protected void buildPlot() {
		this.chart.setBackground(WHITE);
		this.chart.setBackgroundInPlotArea(WHITE);
	}

	/**
	 * Builds X axis.
	 */
	protected void buildXAxis() {}

	/**
	 * Builds Y axis.
	 */
	protected void buildYAxis() {}

	/**
	 * Builds X series.
	 */
	protected void buildXSeries() {}

	/**
	 * Builds Y series.
	 */
	protected void buildYSeries() {}

	/**
	 * Builds legend.
	 *
	 */
	protected void buildLegend() {
		chart.getLegend().setPosition(SWT.RIGHT);
	}

	/**
	 * Builds the chart title.
	 */
	protected void buildTitle() {
		ITitle ctitle = chart.getTitle();
		ctitle.setForeground(BLACK);
		ctitle.setText(this.title);
	}

	/**
	 * Returns the chart instance.
	 *
	 * @return the chart instance
	 */
	public Chart getChart() {
		return chart;
	}

	public void setScale(double scale) {
		this.scale = scale;
		handleUpdateEvent();
	}

	protected double getDoubleValue(Object o) {
		if (o instanceof Integer)
			return ((Integer)o).intValue();
		if (o instanceof Double)
			return ((Double)o).doubleValue();
		return new Double(o.toString()).doubleValue();
	}

	@Override
	public void handleUpdateEvent() {
		repaint();
	}

	protected void repaint() {
		getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				updateDataSet();
            }
		});
	}

}
