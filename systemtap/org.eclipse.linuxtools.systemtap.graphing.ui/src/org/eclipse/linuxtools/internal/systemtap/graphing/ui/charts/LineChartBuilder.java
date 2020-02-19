/****************************************************************
 * Copyright (c) 2006, 2018 IBM Corp. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM - initial API and implementation
 ****************************************************************/
package org.eclipse.linuxtools.internal.systemtap.graphing.ui.charts;

import org.eclipse.linuxtools.systemtap.graphing.core.adapters.IAdapter;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swtchart.ILineSeries;
import org.eclipse.swtchart.ISeries;
import org.eclipse.swtchart.ISeries.SeriesType;
import org.eclipse.swtchart.LineStyle;

/**
 * Builds line chart.
 */

public class LineChartBuilder extends AbstractChartWithAxisBuilder {

	public static final String ID = "org.eclipse.linuxtools.systemtap.graphing.ui.charts.linechartbuilder"; //$NON-NLS-1$

	public LineChartBuilder(Composite parent, int style, String title, IAdapter adapter) {
		super(adapter, parent, style, title);
	}

	@Override
	protected ISeries createChartISeries(int i) {
		ILineSeries series = (ILineSeries) chart.getSeriesSet().createSeries(SeriesType.LINE,
				adapter.getLabels()[i + 1]);
		series.setSymbolColor(COLORS[i % COLORS.length]);
		series.setLineColor(COLORS[i % COLORS.length]);
		series.setLineStyle(LineStyle.SOLID);
		return series;
	}
}
