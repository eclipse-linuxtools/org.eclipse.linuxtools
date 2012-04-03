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

package org.eclipse.linuxtools.systemtap.ui.graphingapi.ui.chart.widget;

import org.eclipse.swt.widgets.Composite;

/**
 * The canvas to draw chart with the tool tip to show the value.
 * 
 * @author Qi Liang
 */
public class ChartWithToolTipCanvas extends ChartCanvas {

	public ChartWithToolTipCanvas(Composite parent, int style) {
		super(parent, style);
	}

	public void regenerateChart() {
		redraw();
	}

	public void repaintChart() {
		redraw();
	}

	public Object peerInstance() {
		return this;
	}

	public Object getContext(Object arg0) {
		return null;
	}

	public Object putContext(Object arg0, Object arg1) {
		return null;
	}

	public Object removeContext(Object arg0) {
		return null;
	}
}
