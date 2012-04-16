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

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.adapters.IAdapter;
import org.eclipse.linuxtools.systemtap.ui.structures.listeners.IUpdateListener;
import org.eclipse.swt.widgets.Display;

import org.swtchart.Chart;
import org.swtchart.ITitle;

/**
 * Provides the common members and the framework to build one chart.
 * 
 * @author Qi Liang
 */
public abstract class AbstractChartBuilder extends Canvas implements IUpdateListener{

	/**
	 * Font name for all titles, labels, and values.
	 */
	protected final static String FONT_NAME = "MS Sans Serif";

	/**
	 * Provides data for chart.
	 */
	protected IAdapter adapter = null;
	protected int xseries;
	protected int[] yseries;

	protected static final Color WHITE = new Color(Display.getDefault(), 255, 255, 255);
	protected static final Color BLACK = new Color(Display.getDefault(), 0, 0, 0);
	protected static final Color RED = new Color(Display.getDefault(), 255, 0, 0);

	/**
	 * Chart instance.
	 */
	protected Chart chart = null;

	/**
	 * Chart title.
	 */
	protected String title = null;

	private class  Painter implements PaintListener {
		/**
		 * The SWT paint callback
		 */
		public void paintControl(PaintEvent pe)
		{
			if (chart == null)
				return;
            Composite co = (Composite) pe.getSource();
			chart.setSize(co.getSize());
		}
	}

	/**
	 * Constructs one chart builder and associate it to one data set.
	 * 
	 * @param dataSet
	 *            data set
	 */

	public AbstractChartBuilder(IAdapter adapter, Composite parent, int style, String title) {
		super(parent, style);
		this.adapter = adapter;
		this.addPaintListener(new Painter());
		this.title = title;
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
	protected void buildXAxis() {

	}

	/**
	 * Builds Y axis.
	 */
	protected void buildYAxis() {

	}

	/**
	 * Builds X series.
	 */
	protected void buildXSeries() {

	}

	/**
	 * Builds Y series.
	 */
	protected void buildYSeries() {

	}

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

	public void updateDataSet() {

	}

	public void setScale(double scale) {

	}

	protected double getDoubleValue(Object o) {
		if (o instanceof Integer)
			return ((Integer)o).intValue();
		if (o instanceof Double)
			return ((Double)o).doubleValue();
		return new Double(o.toString()).doubleValue();
	}
}
