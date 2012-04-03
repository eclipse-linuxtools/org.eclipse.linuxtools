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


import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.linuxtools.systemtap.ui.graphingapi.ui.charts.AbstractChartBuilder;

public class ChartCanvas extends Canvas {
	protected AbstractChartBuilder builder = null;

	public ChartCanvas(Composite parent, int style) {
		super(parent, style | SWT.V_SCROLL | SWT.H_SCROLL);
	}

   	public void handleUpdateEvent() {
   		builder.updateDataSet();
   		this.redraw();
   		this.update();
   	}

   	public synchronized void repaint() {
   		getDisplay().syncExec(new Runnable() {
   			boolean stop = false;
   			public void run() {
   				if(stop) return;
   				try {
   					redraw();
   				} catch (Exception e) {
   					stop = true;
   				}
   			}
   		});
   	}
   }
