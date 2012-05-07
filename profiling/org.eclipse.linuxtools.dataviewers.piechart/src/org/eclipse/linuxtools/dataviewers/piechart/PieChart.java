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

import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.swtchart.Chart;
import org.swtchart.IBarSeries;
import org.swtchart.ISeries;

public class PieChart extends Chart {
	
	public PieChart(Composite parent, int style) {
		super(parent, style);
		for (Control child : getChildren()) {
			if (child.getClass().getName().equals("org.swtchart.internal.axis.AxisTitle") ||
				child.getClass().getName().equals("org.swtchart.internal.PlotArea")) {
				child.setVisible(false); //Don't show original Plot Area and axis
			}
		}
		this.addPaintListener(new PieChartPaintListener(this));
	}

	public void addPaintListener(PaintListener listener)  {
		if (!listener.getClass().getName().startsWith("org.swtchart.internal.axis"))
			super.addPaintListener(listener);
	}	

	public void addPieChartSeries(String labels[], double val[]) {
		for (ISeries s : this.getSeriesSet().getSeries())
			this.getSeriesSet().deleteSeries(s.getId());
		int size = Math.min(labels.length, val.length);
		for (int i=0; i<size; i++) {
			IBarSeries s = (IBarSeries)this.getSeriesSet().createSeries(ISeries.SeriesType.BAR, labels[i]);
			double d[] = new double [1];
			d[0] = val[i];
			s.setXSeries(d);
			s.setBarColor(new Color(this.getDisplay(), IColorsConstants.COLORS[i]));
		}
	}
}
