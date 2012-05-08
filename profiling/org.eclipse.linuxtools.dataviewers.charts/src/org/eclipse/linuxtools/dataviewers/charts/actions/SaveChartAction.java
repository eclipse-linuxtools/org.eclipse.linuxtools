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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.dataviewers.charts.Activator;
import org.eclipse.linuxtools.dataviewers.charts.ChartConstants;
import org.eclipse.linuxtools.dataviewers.charts.view.ChartView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.progress.UIJob;
import org.swtchart.Chart;

/**
 * An action to save a chart as an image (jpeg, gif, png)
 * @author Marzia Maugeri <marzia.maugeri@st.com>
 */
public class SaveChartAction extends Action {

	private FileDialog dialog;
	private Shell shell;
	private Chart cm;
	
	/**
	 * Constructor
	 * 
	 * @param shell the shell used by the dialogs
	 */
	public SaveChartAction(Shell shell,ChartView cview) {
		super("Save chart as...", Activator.getImageDescriptor("icons/save_chart.gif"));
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

		final String ext = dialog.getFilterNames()[dialog.getFilterIndex()];

		UIJob saveAsImage = new UIJob("Save chart as "+file.getName()) {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				int extention;
				if (ext.contains("PNG")){
					extention = SWT.IMAGE_PNG;
				}else if (ext.contains("JPG") || ext.contains("JPEG")) {
					extention = SWT.IMAGE_JPEG;
				}else{
					extention = SWT.IMAGE_PNG;
				}

				try {
					monitor.beginTask("Saving chart as "+file.getName()+"...", IProgressMonitor.UNKNOWN);
					file.createNewFile();
					generateImageFile(file, extention);
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
	
	protected void generateImageFile(File file, int extention){
		Display dsp = Display.getCurrent();
		GC gc = new GC(cm);
		Image img = new Image(dsp, cm.getSize().x, cm.getSize().y);
		gc.copyArea(img, 0, 0);
		gc.dispose();
		ImageLoader imageLoader = new ImageLoader();
		imageLoader.data = new ImageData[] {img.getImageData()};
		imageLoader.save(file.getAbsolutePath(), extention);
	}
	
}
