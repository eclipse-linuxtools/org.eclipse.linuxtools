/*******************************************************************************
 * Copyright (c) 2013 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Ferrazzutti <aferrazz@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.systemtap.graphing.ui.charts;

import org.eclipse.swt.widgets.Composite;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.ISeries;
import org.swtchart.Range;

/**
 * @since 3.0
 */
public class BarChart extends Chart {

    private final static int MIN_LABEL_SIZE = Messages.BarChartBuilder_LabelTrimTag.length();
    private final int fontSize;

    private double[][] actualYSeries;
    private String[] fullLabels = null;
    private IAxis xAxis = null;

    private boolean updateSuspended = false;
    public void suspendUpdate(boolean suspend) {
        if (updateSuspended == suspend) {
            return;
        }
        updateSuspended = suspend;

        // make sure that chart is updated
        if (!suspend) {
            updateLayout();
        }
    }

    public BarChart(Composite parent, int style) {
        super(parent, style);
        fontSize = getFont().getFontData()[0].getHeight();
        xAxis = getAxisSet().getXAxis(0);
        xAxis.enableCategory(true);
        xAxis.setCategorySeries(new String[]{""}); //$NON-NLS-1$
    }

    /**
     * Sets the BarChart's x-axis category labels such that labels won't get
     * cut off if there isn't enough room to display them fully. Use this
     * instead of accessing the chart's x-axis and setting its category
     * series directly.
     * @param series The category series.
     */
    public void setCategorySeries(String[] series) {
        xAxis.setCategorySeries(series);
        fullLabels = xAxis.getCategorySeries();
    }

    /**
     * Return the y-value of the specified series' bar. Use this instead of referring directly
     * to the chart's series values with {@link #getSeriesSet()} and {@link ISeries#getYSeries()}.
     * @param series The index of the bar series to get data from.
     * @param barIndex The index of the bar to get the y-value of.
     * @return The y-value of the specified bar.
     */
    public double getBarValue(int series, int barIndex) {
        return actualYSeries[series][barIndex];
    }

    /**
     * Returns a list of the full (non-trimmed) label names of each bar.
     * Use this instead of accessing the x-axis' category series, which
     * may contain trimmed label names.
     * @return An array containing the names of each bar in the chart.
     */
    public String[] getCategorySeries() {
        String[] copiedCategorySeries = null;

        if (fullLabels != null) {
            copiedCategorySeries = new String[fullLabels.length];
            System.arraycopy(fullLabels, 0, copiedCategorySeries, 0,
                    fullLabels.length);
        }

        return copiedCategorySeries;
    }

    @Override
    public void updateLayout() {
        if (updateSuspended) {
            return;
        }

        // If the x-axis and its labels are set, ensure that their contents fit the width of each label.
        if (fullLabels != null) {
            hideBars();
            String[] labels = xAxis.getCategorySeries();
            if (labels != null && labels.length > 0) {
                String[] trimmedLabels = null;
                trimmedLabels = fitLabels(fullLabels);

                // Only update labels if their trimmed contents are different than their current contents.
                for (int i = 0; i < fullLabels.length; i++) {
                    if (!trimmedLabels[i].equals(labels[i])) {
                        labels = trimmedLabels;
                        break;
                    }
                }
                if (labels == trimmedLabels) {
                    // setCategorySeries triggers an unnecessary call to updateLayout, so prevent it.
                    updateSuspended = true;
                    xAxis.setCategorySeries(labels);
                    updateSuspended = false;
                }
            }
        }
        super.updateLayout();
    }

    /**
     * Given an array of label names, return a new set of names that have been trimmed down
     * in order to fit in the chart axis without getting cut off.
     * @param labels An array of label names that may be trimmed.
     * @return A new array containing label names that have been trimmed to fit in the axis display.
     */
    private String[] fitLabels(String[] labels) {
        Range range = xAxis.getRange();
        int maxLabelSize = (int) Math.max(getClientArea().width / (Math.max(range.upper - range.lower, 1) * fontSize), MIN_LABEL_SIZE);

        String[] trimlabels = new String[labels.length];
        for (int i = 0; i < labels.length; i++) {
            if (labels[i].length() > maxLabelSize) {
                trimlabels[i] = labels[i].substring(0, maxLabelSize - MIN_LABEL_SIZE)
                        .concat(Messages.BarChartBuilder_LabelTrimTag);
            } else {
                trimlabels[i] = labels[i];
            }
        }
        return trimlabels;
    }

    /*
     * Workaround for EBZ #427019: out-of-bounds bars appear, so change their values to
     * keep them properly hidden. Save actual bar values elsewhere so they can be retrieved.
     */
    private void hideBars() {
        Range rangeX = xAxis.getRange();
        double bottomY = getAxisSet().getYAxis(0).getRange().lower;
        double nonNegBottomY = Math.max(0, bottomY);
        ISeries[] allSeries = getSeriesSet().getSeries();
        actualYSeries = new double[allSeries.length][];

        for (int i = 0, n = allSeries.length; i < n; i++) {
            double[] yseries = allSeries[i].getYSeries();
            if (yseries == null) {
                return;
            }

            // Store the true values of the bars into a different array, so they
            // can be displayed later if need be.
            actualYSeries[i] = new double[yseries.length];
            System.arraycopy(yseries, 0, actualYSeries[i], 0, yseries.length);

            for (int x = (int) rangeX.lower; x <= (int) rangeX.upper; x++) {
                if (yseries[x] < bottomY) {
                    yseries[x] = bottomY;
                }
            }
            if (yseries.length - 1 > rangeX.upper) {
                yseries[(int) rangeX.upper + 1] = nonNegBottomY;
            }
            allSeries[i].setYSeries(yseries);
        }
    }

}
