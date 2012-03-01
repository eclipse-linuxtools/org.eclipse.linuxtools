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
import java.io.IOException;

import org.eclipse.birt.chart.device.IDeviceRenderer;
import org.eclipse.birt.chart.exception.ChartException;
import org.eclipse.birt.chart.factory.GeneratedChartState;
import org.eclipse.birt.chart.factory.Generator;
import org.eclipse.birt.chart.factory.RunTimeContext;
import org.swtchart.Chart;
import org.eclipse.birt.chart.model.attribute.Bounds;
import org.eclipse.birt.chart.model.attribute.impl.BoundsImpl;
import org.eclipse.birt.chart.util.PluginSettings;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.dataviewers.charts.Activator;
import org.eclipse.linuxtools.dataviewers.charts.ChartConstants;
import org.eclipse.linuxtools.dataviewers.charts.view.ChartView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import com.ibm.icu.util.ULocale;

/**
 * An action to save a chart as an image (jpeg, gif, png)
 * @author Marzia Maugeri <marzia.maugeri@st.com>
 */
public class SaveChartAction extends Action {

	private FileDialog dialog;
	private Shell shell;
	private Chart cm;
	private IDeviceRenderer idr;
	private Bounds bo;
	private ChartView chartView;
	
	/**
	 * Constructor
	 * 
	 * @param shell the shell used by the dialogs
	 */
	public SaveChartAction(Shell shell,ChartView cview) {
		super("Save chart as...", Activator.getImageDescriptor("icons/save_chart.gif"));
		this.chartView = cview;
		this.setEnabled(false);
		this.shell = shell;
		this.dialog = new FileDialog(shell, SWT.SAVE);
		dialog.setFileName(ChartConstants.DEFAULT_IMG_FILE_NAME);
		dialog.setFilterPath(ChartConstants.DEFAULT_IMG_FILTER_PATH);
		dialog.setFilterExtensions(ChartConstants.saveAsImageExt);
		dialog.setFilterNames(ChartConstants.saveAsImageExtNames);
		dialog.setText("Select an image file (extension will be set to \".jpeg\" if not recognized).");
		
		// restore state if there is one saved
		restoreState();
	}

	/**
	 * Sets the image plugins on the chart and enables the action if chart is not null.
	 * 
	 * @param chart
	 */
	public void setChart(Chart chart) {
		try {
			if (chart != null) {
				setEnabled(true);
			} else {
				setEnabled(false);
			}
			cm = chart;
		} catch (Throwable _)
		{
			Status s = new Status(
					Status.ERROR,
					Activator.PLUGIN_ID,
					Status.ERROR,
					"Error when creating \"save as image\" action...",
					_);
			Activator.getDefault().getLog().log(s);
		}
	}
	
	public void setBounds(Bounds bo){
		this.bo = bo;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() {
		String path = dialog.open();
		if (path == null) {
			// cancel pressed
			return;
		}

		String ext = "";
		

		int dotIdx = path.lastIndexOf(".");
		if (dotIdx > 0) ext = path.substring(dotIdx);
		
		try {
			if (ext.equals(ChartConstants.EXT_GIF)) {
				idr = PluginSettings.instance().getDevice("dv.GIF");
			}
			else if (ext.equals(ChartConstants.EXT_JPEG)) {
				idr = PluginSettings.instance().getDevice("dv.JPEG");
			}
			else if (ext.equals(ChartConstants.EXT_JPG)) {
				idr = PluginSettings.instance().getDevice("dv.JPG");
			}
			else if (ext.equals(ChartConstants.EXT_PNG)) {
				idr = PluginSettings.instance().getDevice("dv.PNG");
			}
			else {
				path += ChartConstants.EXT_JPEG;
				idr = PluginSettings.instance().getDevice("dv.JPG");
			}
		
		} catch (ChartException e) {
			Status s = new Status(
					Status.ERROR,
					Activator.PLUGIN_ID,
					Status.ERROR,
					e.getMessage(),
					e);
			Activator.getDefault().getLog().log(s);
		}

		final File file = new File(path);
		if (file.exists()) {
			boolean overwrite = 
				MessageDialog.openQuestion(shell, "Confirm overwrite", "File already exists. Overwrite?");
			if (overwrite) {
				file.delete();
			} else {
				return;
			}
		}

		Job saveAsImage = new Job("Save chart as "+file.getName()) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					monitor.beginTask("Saving chart as "+file.getName()+"...", IProgressMonitor.UNKNOWN);
					file.createNewFile();
					generateImageFile(file);
					return Status.OK_STATUS;
				} catch (IOException e) {
					return new Status(
							IStatus.ERROR,
							Activator.PLUGIN_ID,
							"Error saving chart to \"" 
							+ file.getAbsolutePath()
							+ "\":"
							+ e.getMessage(),
							e);
				}
			}
		};
		saveAsImage.setUser(true);
		saveAsImage.schedule();

		// save the state of the dialog
		saveState();
	}

	/**
	 * Restores the state of this action (file dialog)
	 */
	public void restoreState() {
		try {
			IDialogSettings settings = 
				Activator.getDefault().getDialogSettings().getSection(ChartConstants.TAG_SECTION_BIRTCHARTS_SAVEACTION_STATE);
			if (settings == null) {
				settings = Activator.getDefault().getDialogSettings().addNewSection(ChartConstants.TAG_SECTION_BIRTCHARTS_SAVEACTION_STATE);
				return;
			}

			dialog.setFileName(settings.get(ChartConstants.TAG_IMG_FILE_NAME));
			dialog.setFilterPath(settings.get(ChartConstants.TAG_IMG_FILTER_PATH));
		}
		catch (Exception e) {
			Status s = new Status(
					Status.ERROR,
					Activator.PLUGIN_ID,
					Status.ERROR,
					e.getMessage(),
					e);
			Activator.getDefault().getLog().log(s);
		}
	}

	/**
	 * Saves the state of this action (file dialog)
	 */
	public void saveState() {
		try {
			IDialogSettings settings = 
				Activator.getDefault().getDialogSettings().getSection(ChartConstants.TAG_SECTION_BIRTCHARTS_SAVEACTION_STATE);
			if (settings == null) {
				settings = Activator.getDefault().getDialogSettings().addNewSection(ChartConstants.TAG_SECTION_BIRTCHARTS_SAVEACTION_STATE);
			}

			settings.put(ChartConstants.TAG_IMG_FILE_NAME, dialog.getFileName());
			settings.put(ChartConstants.TAG_IMG_FILTER_PATH, dialog.getFilterPath());
		}
		catch (Exception e) {
			Status s = new Status(
					Status.ERROR,
					Activator.PLUGIN_ID,
					Status.ERROR,
					e.getMessage(),
					e);
			Activator.getDefault().getLog().log(s);
		}
	}
	
	protected void generateImageFile(File file){
		RunTimeContext rtc = new RunTimeContext( );
		rtc.setULocale( ULocale.getDefault( ) );

		final Generator gr = Generator.instance( );
		GeneratedChartState gcs = null;
	
		Bounds boFile = null;
//		bo = chartView.getChartViewer().getBounds();
		//Set the chart size
		if (bo != null){
			boFile = BoundsImpl.create(bo.getLeft(), bo.getTop(), bo.getWidth(), bo.getHeight());
		}
		else{
			boFile = BoundsImpl.create(0, 0, 800, 600);
		}
			
		
		/*try {
			gcs = gr.build( idr.getDisplayServer( ), cm, boFile, null, rtc, null );
		} catch (ChartException e) {
			Status s = new Status(
					Status.ERROR,
					Activator.PLUGIN_ID,
					Status.ERROR,
					e.getMessage(),
					e);
			Activator.getDefault().getLog().log(s);
		}*/

		//Specify the file to write to. 
		idr.setProperty( IDeviceRenderer.FILE_IDENTIFIER, file.getAbsolutePath() );

		//generate the chart
		try {
			gr.render( idr, gcs );
		} catch (ChartException e) {
			Status s = new Status(
					Status.ERROR,
					Activator.PLUGIN_ID,
					Status.ERROR,
					e.getMessage(),
					e);
			Activator.getDefault().getLog().log(s);
		}
	}
	
	
}
