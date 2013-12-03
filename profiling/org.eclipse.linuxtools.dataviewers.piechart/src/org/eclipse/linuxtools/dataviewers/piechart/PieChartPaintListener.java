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
import org.swtchart.IBarSeries;
import org.swtchart.ISeries;
import org.swtchart.Range;

public class PieChartPaintListener implements PaintListener {

    private PieChart chart;
    private Control plotArea;
    private double[][] seriesValues;
    private String[] seriesNames;
    private static final int X_GAP = 10;

    private static final Color WHITE = Display.getDefault().getSystemColor(SWT.COLOR_WHITE);
    private static final Color BLACK = Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
    private static final String FONT = "Arial"; //$NON-NLS-1$

    /**
     * Handles drawing & updating of a PieChart, with titles given to its legend and
     * to each of its pies. Pies will be drawn in the given chart's plot area.
     * @param chart The PieChart to draw & update.
     * @since 2.0
     */
    public PieChartPaintListener(PieChart chart) {
        this.chart = chart;
        this.plotArea = chart.getPlotArea();
    }

    @Override
    public void paintControl(PaintEvent e) {
        GC gc = e.gc;
        Rectangle bounds;
        this.getPieSeriesArray();
        if (seriesValues.length == 0) {
            bounds = gc.getClipping();
            Font font = new Font(Display.getDefault(), FONT, 15, SWT.BOLD);
            gc.setForeground(BLACK);
            gc.setFont(font);
            String text = "No data"; //$NON-NLS-1$
            Point textSize = e.gc.textExtent(text);
            gc.drawText(text, (bounds.width - textSize.x) / 2, (bounds.height - textSize.y) / 2);
            font.dispose();
            return;
        }
        bounds = plotArea.getBounds();
        setTitleBounds(bounds);
        int width = bounds.width / seriesValues.length;
        int x = bounds.x;

        if (chart.getLegend().isVisible()) {
            Rectangle legendBounds = ((Control) chart.getLegend()).getBounds();
            Font font = new Font(Display.getDefault(), FONT, 10, SWT.BOLD);
            gc.setForeground(BLACK);
            gc.setFont(font);
            String text = chart.getAxisSet().getXAxis(0).getTitle().getText();
            Point textSize = e.gc.textExtent(text);
            gc.drawText(text, legendBounds.x + (legendBounds.width - textSize.x) / 2, legendBounds.y - textSize.y);
            font.dispose();
        }

        for (int i = 0; i < seriesValues.length; i++) {
            drawPieChart(e, i, new Rectangle(x, bounds.y, width, bounds.height));
            x += width;
        }
    }

    private void setTitleBounds(Rectangle bounds) {
        Control title = (Control) chart.getTitle();
        Rectangle titleBounds = title.getBounds();
        title.setLocation(new Point(bounds.x + (bounds.width - titleBounds.width) / 2, title.getLocation().y));
    }

    private void drawPieChart(PaintEvent e, int chartnum, Rectangle bounds) {
        double series[] = seriesValues[chartnum];
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
                gc.setBackground(((IBarSeries) chart.getSeriesSet().getSeries()[i]).getBarColor());

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

        Font font = new Font(Display.getDefault(), FONT, 12, SWT.BOLD);
        gc.setForeground(BLACK);
        gc.setBackground(WHITE);
        gc.setFont(font);
        String text = seriesNames[chartnum];
        Point textSize = e.gc.textExtent(text);
        gc.drawText(text, pieX + (pieWidth - textSize.x) / 2, pieY + pieWidth + textSize.y);
        font.dispose();
    }

    private void getPieSeriesArray() {
        ISeries series[] = this.chart.getSeriesSet().getSeries();
        if (series == null || series.length == 0) {
            seriesValues = new double[0][0];
            seriesNames = new String[0];
		    return;
		}
        String names[] = this.chart.getAxisSet().getXAxis(0).getCategorySeries();
        Range range = chart.getAxisSet().getXAxis(0).getRange();
        int itemRange = (int) range.upper - (int) range.lower + 1;
        int itemOffset = (int) range.lower;
        seriesValues = new double[itemRange][series.length];
        seriesNames = new String[itemRange];

        for (int i = 0; i < seriesValues.length; i++) {
            seriesNames[i] = names[i + itemOffset];
            for (int j = 0; j < seriesValues[i].length; j++) {
                double d[] = series[j].getXSeries();
                if (d != null && d.length > 0) {
                    seriesValues[i][j] = d[i + itemOffset];
                } else {
                    seriesValues[i][j] = 0;
                }
            }
        }

        return;
    }

}
