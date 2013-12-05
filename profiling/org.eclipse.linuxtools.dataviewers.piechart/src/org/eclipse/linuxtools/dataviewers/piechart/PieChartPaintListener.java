/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Renato Stoffalette Joao <rsjoao@br.ibm.com>
 *******************************************************************************/
package org.eclipse.linuxtools.dataviewers.piechart;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.swtchart.ISeries;

public class PieChartPaintListener implements PaintListener {

    private PieChart chart;
    private Control plotArea;
    private String[] seriesNames;
    private static final int X_GAP = 10;

    private static final Color WHITE = Display.getDefault().getSystemColor(SWT.COLOR_WHITE);
    private static final Color BLACK = Display.getDefault().getSystemColor(SWT.COLOR_BLACK);

    /**
     * Draws pie charts with no titles given to each pie.
     * @param chart
     * @param plotArea
     */
    public PieChartPaintListener(PieChart chart, Control plotArea) {
        this(chart, plotArea, new String[0]);
    }

    /**
     * Handles drawing & updating of a PieChart, with titles given to each of its pies.
     * @param chart The PieChart to draw & update.
     * @param plotArea The area in which to draw the pies.
     * @param seriesNames The titles given to individual pies.
     * @since 1.1
     */
    public PieChartPaintListener(PieChart chart, Control plotArea, String[] seriesNames) {
        this.chart = chart;
        this.plotArea = plotArea;
        this.seriesNames = seriesNames;
    }

    @Override
    public void paintControl(PaintEvent e) {
        GC gc = e.gc;
        Rectangle bounds;
        if (plotArea == null) {
			bounds = gc.getClipping();
		} else {
			bounds = plotArea.getBounds();
		}
        double[][] series = this.getPieSeriesArray();
        if (series.length == 0) {
            Rectangle allBounds = chart.getBounds();
            Font font = new Font(Display.getDefault(), "Arial", 15, SWT.BOLD); //$NON-NLS-1$
            gc.setForeground(BLACK);
            gc.setFont(font);
            String text = "No data"; //$NON-NLS-1$
            Point textSize = e.gc.textExtent(text);
            gc.drawText(text, (allBounds.width - textSize.x) / 2, (allBounds.height -  textSize.y) / 2);
            font.dispose();
            return;
        }
        int width = (bounds.width - bounds.x) / series.length;
        int x = bounds.x;

        for (int i = 0; i < series.length; i++) {
            double[] s = series[i];
            drawPieChart(e, i, s, new Rectangle(x, bounds.y, width, bounds.height));
            x += width;
        }
    }

    private void drawPieChart(PaintEvent e, int chartnum, double series[], Rectangle bounds) {
        int nelemSeries = series.length;
        double sumTotal = 0;

        for (int i = 0; i < nelemSeries; i++) {
            sumTotal += series[i];
        }

        GC gc = e.gc;
        gc.setLineWidth(1);

        int pieWidth = Math.min(bounds.width - X_GAP, bounds.height);
        int pieX = bounds.x + (bounds.width - pieWidth) / 2;
        int pieY = bounds.y + (bounds.height - pieWidth) / 2;
        if (sumTotal == 0) {
			gc.drawOval(pieX, pieY, pieWidth, pieWidth);
		} else {
            double factor = 100 / sumTotal;
            int sweepAngle = 0;
            int incrementAngle = 0;
            int initialAngle = 90;
            for (int i = 0; i < nelemSeries; i++) {
                gc.setBackground(new Color(e.display, chart.sliceColor(i)));

                if (i == (nelemSeries - 1)) {
					sweepAngle = 360 - incrementAngle;
				} else {
                    double angle = series[i] * factor * 3.6;
                    sweepAngle = (int) Math.round(angle);
                }
                gc.fillArc(pieX, pieY, pieWidth, pieWidth, initialAngle, (-sweepAngle));
                gc.drawArc(pieX, pieY, pieWidth, pieWidth, initialAngle, (-sweepAngle));
                incrementAngle += sweepAngle;
                initialAngle += (-sweepAngle);
            }
        }
        if (chartnum < seriesNames.length) {
            Font font = new Font(Display.getDefault(), "Arial", 15, SWT.BOLD); //$NON-NLS-1$
            gc.setForeground(BLACK);
            gc.setBackground(WHITE);
            gc.setFont(font);
            String text = seriesNames[chartnum];
            Point textSize = e.gc.textExtent(text);
            gc.drawText(text, pieX + (pieWidth - textSize.x) / 2, pieY + pieWidth + textSize.y);
            font.dispose();
        }
    }

    private double[][] getPieSeriesArray() {
        ISeries series[] = this.chart.getSeriesSet().getSeries();
        if (series == null || series.length == 0) {
			return new double[0][0];
		}
        double result[][] = new double[series[0].getXSeries().length][series.length];

        for (int i = 0; i < result.length; i++) {
            for (int j = 0; j < result[i].length; j++) {
                double d[] = series[j].getXSeries();
                if (d != null && d.length > 0) {
					result[i][j] = d[i];
				} else {
					result[i][j] = 0;
				}
            }
        }

        return result;
    }
}
