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

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.swtchart.ISeries;

public class PieChartPaintListener implements PaintListener {

	private PieChart chart;

	public PieChartPaintListener(PieChart chart) {
		this.chart = chart;
	}

	public void paintControl(PaintEvent e) {
		int nelemSeries = 0;
		double sumTotal = 0;
    	int sweepAngle=0;
		int incrementAngle=0;
		int initialAngle=90;	
		double[] series = this.getPieSeriesArray();
		nelemSeries = series.length;
		for(int i = 0; i < nelemSeries; i++){
			sumTotal += series[i];			
		}

		double factor = 100 / sumTotal;

		GC gc = e.gc;
		Rectangle bounds = gc.getClipping();		
		int pieWidth = Math.min(bounds.width, bounds.height)/2;
		gc.setLineWidth(1);
		
		for (int i=0; i < nelemSeries; i++) {
			gc.setBackground(new Color(e.display, IColorsConstants.COLORS[i]));

			if (i==(nelemSeries-1))
				sweepAngle = 360 - incrementAngle;
			else {
				double angle = series[i] * factor * 3.6;
				sweepAngle = (int) Math.round(angle);
			}
			gc.fillArc(bounds.width/3,bounds.height/4,pieWidth,pieWidth,initialAngle,(-sweepAngle));
			gc.drawArc(bounds.width/3,bounds.height/4,pieWidth,pieWidth,initialAngle,(-sweepAngle));
			incrementAngle +=sweepAngle;
			initialAngle += (-sweepAngle);
		}
	}

	private double[] getPieSeriesArray() {
		ISeries series[] = this.chart.getSeriesSet().getSeries();
		double result[] = new double[series.length];

		for (int i=0; i<result.length; i++) {
			double d[] = series[i].getXSeries();
			if (d != null && d.length > 0)
				result[i] = d[0];
			else
				result[i] = 0;
		}

		return result;
	}
}
