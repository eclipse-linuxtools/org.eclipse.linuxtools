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
import org.eclipse.linuxtools.internal.systemtap.graphingapi.ui.preferences.GraphingAPIPreferenceConstants;
import org.eclipse.linuxtools.systemtap.graphingapi.core.adapters.IAdapter;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.swtchart.IAxis;
import org.swtchart.ISeries;
import org.swtchart.ITitle;
import org.swtchart.LineStyle;
import org.swtchart.Range;

/**
 * Builds the chart with axis.
 *
 * @author Qi Liang
 */
public abstract class AbstractChartWithAxisBuilder extends AbstractChartBuilder {

	private PaintListener titleBoundsPaintListener;
	private double defaultMargin = 0.04;
	/**
	 * @since 3.0
	 */
	protected double getChartMarginXL() {
		return defaultMargin;
	}
	/**
	 * @since 3.0
	 */
	protected double getChartMarginXU() {
		return defaultMargin;
	}
	/**
	 * @since 3.0
	 */
	protected double getChartMarginYL() {
		return defaultMargin;
	}
	/**
	 * @since 3.0
	 */
	protected double getChartMarginYU() {
		return defaultMargin;
	}

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

	@Override
	protected void createChart() {
		super.createChart();
		applyTitleBoundsListener();
		chartMouseMoveListener = new ChartWithAxisMouseMoveListener(chart, chart.getPlotArea());
	}

    /**
     * After this method is called, the chart's title will (from then on) be centred with the plot area.
	 * @since 3.0
	 */
    protected void applyTitleBoundsListener() {
    	titleBoundsPaintListener = new PaintListener() {

			@Override
			public void paintControl(PaintEvent e) {
				Rectangle bounds = chart.getPlotArea().getBounds();
				Control title = (Control) chart.getTitle();
		        Rectangle titleBounds = title.getBounds();
		        title.setLocation(new Point(bounds.x + (bounds.width - titleBounds.width) / 2, title.getLocation().y));
			}
		};
		chart.addPaintListener(titleBoundsPaintListener);
    }

    /**
     * Removes the chart's paint listener for repositioning the title, if one has been applied.
     * @since 3.0
     */
    protected void removeTitleBoundsListener() {
    	if (titleBoundsPaintListener != null) {
	    	chart.removePaintListener(titleBoundsPaintListener);
	    	titleBoundsPaintListener = null;
    	}
    }

	/**
	 * Builds X axis.
	 */
	@Override
	protected void buildXAxis() {
		String labels[] = adapter.getLabels();
		IAxis xAxis = this.chart.getAxisSet().getXAxis(0);
		if (xLineGrid) {
			xAxis.getGrid().setStyle(LineStyle.SOLID);
		} else {
			xAxis.getGrid().setStyle(LineStyle.NONE);
		}
		xAxis.getTick().setForeground(BLACK);
		ITitle xTitle = xAxis.getTitle();
		xTitle.setForeground(BLACK);

		if (labels.length > 0) {
			xTitle.setText(labels[0]);
		}
		else {
			xTitle.setText(""); //$NON-NLS-1$
		}
	}

	/**
	 * Builds Y axis.
	 */
	@Override
	protected void buildYAxis() {
		IAxis yAxis = this.chart.getAxisSet().getYAxis(0);
		yAxis.getTitle().setText(""); //$NON-NLS-1$
		if (yLineGrid) {
			yAxis.getGrid().setStyle(LineStyle.SOLID);
		} else {
			yAxis.getGrid().setStyle(LineStyle.NONE);
		}
		yAxis.getTick().setForeground(BLACK);
	}

	/**
	 * Builds X series.
	 */
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

		Double[] all_valx = new Double[len];
		Double[][] all_valy = new Double[leny][len];
		// Will want to centre view around points, so be as accurate with max/min as possible.
		double maxX = Double.NEGATIVE_INFINITY;
		double maxY = maxX;
		double minX = Double.POSITIVE_INFINITY;
		double minY = minX;

		// Read in from the data array all x/y points to plot.
		// In the case of an empty (null) value in either axis, ignore both x & y axis data for that point.
		for (int i = 0; i < len; i++) {
			for (int j = 0; j < leny + 1; j++) {
				Double val = getDoubleOrNullValue(data[start + i][j]);
				if (j == 0) {
					if (val != null) {
						all_valx[i] = val;
						maxX = Math.max(val, maxX);
						minX = Math.min(val, minX);
					} else {
						break;
					}
				} else if (val != null) {
					all_valy[j-1][i] = val;
					maxY = Math.max(val, maxY);
					minY = Math.min(val, minY);
				}
			}
		}

		// Now create dense arrays of x/y values that exclude null values,
		// and plot those values to the chart.

		ISeries allSeries[] = chart.getSeriesSet().getSeries();
		ISeries series = null;
		for (int i = 0; i < leny; i++) {
			if (i >= allSeries.length) {
				series = createChartISeries(i);
			} else {
				series = chart.getSeriesSet().getSeries()[i];
			}

			double[] valx = new double[len];
			double[] valy = new double[len];
			int len_trim = 0;
			for (int j = 0; j < len; j++) {
				if (all_valx[j] != null && all_valy[i][j] != null) {
					valx[len_trim] = all_valx[j].doubleValue();
					valy[len_trim] = all_valy[i][j].doubleValue();
					len_trim++;
				}
			}
			double[] valx_trim = new double[len_trim];
			double[] valy_trim = new double[len_trim];
			for (int j = 0; j < len_trim; j++) {
				valx_trim[j] = valx[j];
				valy_trim[j] = valy[j];
			}
			series.setXSeries(valx_trim);
			series.setYSeries(valy_trim);
		}

		if (series != null && series.getXSeries().length > 0) {
			applyRangeX(minX, maxX);
			applyRangeY(minY, maxY);
		}
		chart.redraw();
	}

	/**
	 * This updates the visible range of the chart's x-axis.
	 */
	private void applyRangeX(double min, double max) {
		IAxis axis = chart.getAxisSet().getXAxis(0);
		double actualRange = max - min;
		double scaledRange = actualRange * scale;
		double marginL = scaledRange > 0 ? scaledRange * getChartMarginXL() : 1;
		double marginU = scaledRange > 0 ? scaledRange * getChartMarginXU() : 1;

		double lower = (actualRange - scaledRange) * scroll + min;
		axis.setRange(new Range(lower - marginL, lower + scaledRange + marginU));
	}

	/**
	 * This updates the visible range of the chart's y-axis.
	 * @since 3.0
	 */
	protected void applyRangeY(double min, double max) {
		IAxis axis = chart.getAxisSet().getYAxis(0);
		double actualRange = max - min;
		double scaledRange = actualRange * scaleY;
		double marginL = scaledRange > 0 ? scaledRange * getChartMarginYL() : 1;
		double marginU = scaledRange > 0 ? scaledRange * getChartMarginYU() : 1;

		double lower = (actualRange - scaledRange) * scrollY + min;
		axis.setRange(new Range(lower - marginL, lower + scaledRange + marginU));
	}

	@Override
	public void updateDataSet() {
		buildXSeries();
		chartMouseMoveListener.update();
	}
}
