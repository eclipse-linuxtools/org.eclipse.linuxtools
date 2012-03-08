/*******************************************************************************
 * Copyright (c) 2009 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marzia Maugeri <marzia.maugeri@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.dataviewers.charts.actions;

import java.io.File;
import java.io.FileInputStream;

import org.eclipse.birt.chart.model.Chart;
import org.eclipse.birt.chart.model.Serializer;
import org.eclipse.birt.chart.model.impl.SerializerImpl;
import org.eclipse.birt.core.ui.frameworks.taskwizard.WizardBase;
import org.eclipse.jface.action.Action;
import org.eclipse.linuxtools.dataviewers.charts.UIHelper;
import org.eclipse.linuxtools.dataviewers.charts.view.ChartView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;



public class OpenChartAction extends Action {
	private Chart chart = null;
	private FileDialog dialog;
	private ChartView chartView;
	
	public OpenChartAction(Shell shell,ChartView cView){
		setImageDescriptor( UIHelper.getImageDescriptor( "icons/eimport.gif" ) ); 
		setDisabledImageDescriptor( UIHelper.getImageDescriptor( "icons/dimport.gif" ) );
		setToolTipText( "Open XML Source" );
		setEnabled(true);
		this.chartView = cView;
		this.dialog = new FileDialog(shell, SWT.OPEN);
		dialog.setText("Select a chart file ");
		dialog.setFilterExtensions(new String[]{"*.chart"});
	}
	
	public void run() {
		String path = dialog.open();
		if (path == null) {
			// cancel pressed
			return;
		}
		
		File chartFile = new File( path );
		// Reads the chart model
		try {
			Serializer serializer = SerializerImpl.instance( );
			if (chartFile.exists()) {
				chart = serializer.read(new FileInputStream(chartFile));
				chartView.getChartViewer().setBuffer(null);
				chartView.setChart(chart);
			}
		}
		catch ( Exception e ) {
			WizardBase.displayException( e );
		}
	}
	
	public Chart getChart(){
		return chart;
	}

}
