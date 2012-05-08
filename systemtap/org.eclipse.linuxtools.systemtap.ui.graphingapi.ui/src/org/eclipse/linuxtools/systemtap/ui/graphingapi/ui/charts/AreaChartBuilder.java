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

import org.eclipse.swt.widgets.Composite;

import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.adapters.IAdapter;

import org.swtchart.ILineSeries;
import org.swtchart.ISeries;

/**
 * Builds bar chart.
 * 
 * @author Qi Liang
 */

public class AreaChartBuilder extends LineChartBuilder {

	public static final String ID = "org.eclipse.linuxtools.systemtap.ui.graphingapi.ui.charts.areachartbuilder";

    public AreaChartBuilder(Composite parent, int style, String title,IAdapter adapter) {
		super(parent, style, title, adapter);
    }
    
	protected ISeries createChartISeries(int i) {
		ILineSeries series = (ILineSeries) super.createChartISeries(i);
		series.enableArea(true);
		return series;
	}
}
