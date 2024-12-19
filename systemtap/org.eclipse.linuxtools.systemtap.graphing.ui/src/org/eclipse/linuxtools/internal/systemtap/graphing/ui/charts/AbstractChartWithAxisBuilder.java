/*******************************************************************************
 * Copyright (c) 2010, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *    Red Hat Inc. - modified to handle SWTChart 0.9.0 vs 0.8.0; ongoing maintenance
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.graphing.ui.charts;

import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.linuxtools.internal.systemtap.graphing.ui.charts.listeners.ChartWithAxisMouseMoveListener;
import org.eclipse.linuxtools.internal.systemtap.graphing.ui.preferences.GraphingPreferenceConstants;
import org.eclipse.linuxtools.systemtap.graphing.core.adapters.IAdapter;
import org.eclipse.linuxtools.systemtap.graphing.ui.charts.AbstractChartBuilder;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swtchart.IAxis;
import org.eclipse.swtchart.ISeries;
import org.eclipse.swtchart.ITitle;
import org.eclipse.swtchart.LineStyle;
import org.eclipse.swtchart.Range;

/**
 * A {@link AbstractChartBuilder} for building a chart with axes.
 * @author Qi Liang
 */
public abstract class AbstractChartWithAxisBuilder extends AbstractChartBuilder {

    private PaintListener titleBoundsPaintListener;
    private double defaultMargin = 0.04;
    /**
     * @return The size of the chart's left margin.
     * @since 3.0
     */
    protected double getChartMarginXL() {
        return defaultMargin;
    }
    /**
     * @return The size of the chart's right margin.
     * @since 3.0
     */
    protected double getChartMarginXU() {
        return defaultMargin;
    }
    /**
     * @return The size of the chart's top margin.
     * @since 3.0
     */
    protected double getChartMarginYL() {
        return defaultMargin;
    }
    /**
     * @return The size of the chart's bottom margin.
     * @since 3.0
     */
    protected double getChartMarginYU() {
        return defaultMargin;
    }

    protected boolean xLineGrid, yLineGrid;
    /**
     * @since 3.0
     */
    protected int xSeriesTicks, ySeriesTicks;

    /**
     * Creates a chart series for this chart.
     * @param i The index of the series to create.
     * @return The newly created series.
     */
	protected abstract ISeries<?> createChartISeries(int i);

    @Override
    protected void updateProperties(PropertyChangeEvent event) {
        super.updateProperties(event);
        String eventName = event.getProperty();
        if (eventName.equals(GraphingPreferenceConstants.P_SHOW_X_GRID_LINES)) {
            xLineGrid = store.getBoolean(GraphingPreferenceConstants.P_SHOW_X_GRID_LINES);
            buildXAxis();
        } else if (eventName.equals(GraphingPreferenceConstants.P_SHOW_Y_GRID_LINES)) {
            yLineGrid = store.getBoolean(GraphingPreferenceConstants.P_SHOW_Y_GRID_LINES);
            buildYAxis();
        } else if (eventName.equals(GraphingPreferenceConstants.P_X_SERIES_TICKS)) {
            xSeriesTicks = store.getInt(GraphingPreferenceConstants.P_X_SERIES_TICKS);
            buildXAxis();
        } else if (eventName.equals(GraphingPreferenceConstants.P_Y_SERIES_TICKS)) {
            ySeriesTicks = store.getInt(GraphingPreferenceConstants.P_Y_SERIES_TICKS);
            buildYAxis();
        }
    }

    /**
     * Constructs a builder for a chart with axes and associates it to one data set.
     * @param adapter An {@link IAdapter} for reading from the chart's data set.
     * @param parent The parent {@link Composite} that will contain this chart builder.
     * @param style The style of the chart to construct.
     * @param title The title of the chart to construct.
     */
    public AbstractChartWithAxisBuilder(IAdapter adapter, Composite parent, int style, String title) {
        super(adapter, parent, style, title);
        xLineGrid = store.getBoolean(GraphingPreferenceConstants.P_SHOW_X_GRID_LINES);
        yLineGrid = store.getBoolean(GraphingPreferenceConstants.P_SHOW_Y_GRID_LINES);
        xSeriesTicks = store.getInt(GraphingPreferenceConstants.P_X_SERIES_TICKS);
        ySeriesTicks = store.getInt(GraphingPreferenceConstants.P_Y_SERIES_TICKS);
    }

    @Override
    protected void createChart() {
        super.createChart();
        applyTitleBoundsListener();
		chartMouseMoveListener = new ChartWithAxisMouseMoveListener(chart, chart.getPlotArea().getControl());
    }

    /**
     * After this method is called, the chart's title will (from then on) be centered with the plot area.
     * @since 3.0
     */
    protected void applyTitleBoundsListener() {
        ITitle title = chart.getTitle();
        // Underlying SWT Chart implementation changes from the title being a Control to just
        // a PaintListener.  In the Control class case, we can move it's location to
        // center over a PieChart, but in the latter case, we need to alter the title
        // with blanks in the PaintListener and have the title paint after it
        // once the title has been altered.
        if (title instanceof Control) {
            titleBoundsPaintListener = e -> {
			    Rectangle bounds = chart.getPlotArea().getBounds();
			    Control title1 = (Control) chart.getTitle();
			    Rectangle titleBounds = title1.getBounds();
			    title1.setLocation(new Point(bounds.x + (bounds.width - titleBounds.width) / 2, title1.getLocation().y));
			};
            chart.addPaintListener(titleBoundsPaintListener);
        } else {
            // move title paint listener to end
            chart.removePaintListener((PaintListener)title);
            titleBoundsPaintListener = e -> {
			    ITitle title1 = chart.getTitle();
			    Font font = title1.getFont();
			    Font oldFont = e.gc.getFont();
			    e.gc.setFont(font);
			    Control legend = (Control)chart.getLegend();
			    Rectangle legendBounds = legend.getBounds();
			    int adjustment = legendBounds.width - 15;
			    Point blankSize = e.gc.textExtent(" "); //$NON-NLS-1$
			    int numBlanks = ((adjustment / blankSize.x) >> 1) << 1;
			    String text = title1.getText().trim();
			    for (int i = 0; i < numBlanks; ++i) {
			        text += " "; //$NON-NLS-1$
			    }
			    e.gc.setFont(oldFont);
			    title1.setText(text);
			};
            chart.addPaintListener(titleBoundsPaintListener);
            chart.addPaintListener((PaintListener)title);
        }
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
        xAxis.getTick().setTickMarkStepHint(xSeriesTicks);
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
        yAxis.getTick().setTickMarkStepHint(ySeriesTicks);
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

		ISeries<?> allSeries[] = chart.getSeriesSet().getSeries();
		ISeries<?> series = null;
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
     * Updates the visible range of the chart's x-axis.
     * @param min The smallest x-value that should be in range.
     * @param max The largest x-value that should be in range.
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
     * Updates the visible range of the chart's y-axis.
     * @param min The smallest y-value that should be in range.
     * @param max The largest y-value that should be in range.
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

    @Override
    protected void buildYSeries() {
    }
}
