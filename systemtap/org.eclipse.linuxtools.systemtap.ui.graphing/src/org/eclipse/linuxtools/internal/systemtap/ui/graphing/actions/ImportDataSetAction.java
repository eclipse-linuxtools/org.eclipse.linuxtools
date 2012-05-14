/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.graphing.actions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.linuxtools.internal.systemtap.ui.graphing.Localization;
import org.eclipse.linuxtools.systemtap.ui.graphing.GraphingPerspective;
import org.eclipse.linuxtools.systemtap.ui.graphing.views.GraphSelectorView;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.datasets.IDataSet;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.ui.wizards.dataset.DataSetFactory;
import org.eclipse.linuxtools.systemtap.ui.logging.LogManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;



/**
 * This action is designed to open up an exported <code>DataSet</code> from the graphing.
 * It allows users to bring up data from previous runs of stap so they can 
 * see old data.  Importing DataSets is no different then a script live and getting data.
 * @author Ryan Morse
 */
public class ImportDataSetAction extends Action implements IWorkbenchWindowActionDelegate {
	public void init(IWorkbenchWindow window) {
		LogManager.logDebug("Start ImportDataSetAction.init", this);
		LogManager.logDebug("Initializing", this);
		fWindow = window;
		LogManager.logDebug("End ImportDataSetAction.init", this);
	}

	/**
	 * This is the main method of the action.  It handles prompting the user
	 * for a file that contans an exported DataSet.  Then, it will generate
	 * a new <code>DataSet</code> to hold all of the data.
	 * @param act The action that fired this method.
	 */
	public void run(IAction act) {
		LogManager.logDebug("Start ImportDataSetAction.run", this);
		//Get the file
		FileDialog dialog= new FileDialog(fWindow.getShell(), SWT.OPEN);
		dialog.setText(Localization.getString("ImportDataSetAction.OpenDataSetFile"));
		String fileName = dialog.open();
		
		File f = null;
		
		if(null == fileName || fileName.length() <= 0)
			return;
		
		f = new File(fileName);

		if(!f.exists() || !f.canRead())
			return;
		
		//Create a new DataSet
		IDataSet dataSet = readFile(f);
		
		if(null == dataSet) {
			displayError(Localization.getString("ImportDataSetAction.ErrorReadingDataSet"));
			return;
		}
		
		//Create a new script set
		try {
			IWorkbenchPage p = PlatformUI.getWorkbench().showPerspective(GraphingPerspective.ID, PlatformUI.getWorkbench().getActiveWorkbenchWindow());
			IViewPart ivp = p.findView(GraphSelectorView.ID);
			((GraphSelectorView)ivp).createScriptSet(fileName, dataSet);
		} catch(WorkbenchException we) {
			LogManager.logCritical("WorkbenchException ImportDataSetAction.run:" + we.getMessage(), this);
		}
		LogManager.logDebug("End ImportDataSetAction.run", this);
	}

	public void selectionChanged(IAction a, ISelection s) {}
	
	/**
	 * Read the contents of the file into a new DataSet
	 * @param f The file that was selected to read a DataSet from
	 * @return The newly created DataSet containing the data from the file.
	 */
	private IDataSet readFile(File f) {
		LogManager.logDebug("Start ImportDataSetAction.readFile", this);
		IDataSet data;

		readHeader(f);
		if(null == labels || null == id)
			return null;
			
		data = DataSetFactory.createFilteredDataSet(id, labels);
		data.readFromFile(f);
		
		LogManager.logDebug("End ImportDataSetAction.readFile", this);
		return data;
	}
	
	/**
	 * This method will read out the labels and DataSet type from the file
	 * @param f The file that was selected for reading.
	 * @return An array of all of the labels found in the file
	 */
	private void readHeader(File f) {
		LogManager.logDebug("Start ImportDataSetAction.readLabels", this);

		try {
			FileReader fr = new FileReader(f);
			BufferedReader br = new BufferedReader(fr);
			
			id = br.readLine();
			
			String line = br.readLine();
			br.close();
			labels = line.split(", ");
		} catch(FileNotFoundException fnfe) {
			LogManager.logCritical("FileNotFoundException ImportDataSetAction.readLabels:" + fnfe.getMessage(), this);
		} catch(IOException ioe) {
			LogManager.logCritical("IOException ImportDataSetAction.readLabels:" + ioe.getMessage(), this);
		}

		LogManager.logDebug("End ImportDataSetAction.readLabels", this);
	}
	
	private void displayError(String message) {
		LogManager.logInfo("Initializing", MessageDialog.class);
		MessageDialog.openWarning(fWindow.getShell(), Localization.getString("ImportDataSetAction.Problem"), message);
		LogManager.logInfo("Disposing", MessageDialog.class);
	}
	
	public void dispose() {
		LogManager.logDebug("Start ImportDataSetAction.dispose", this);
		LogManager.logInfo("Disposing", this);
		fWindow = null;
		LogManager.logDebug("End ImportDataSetAction.dispose", this);
	}
	
	private IWorkbenchWindow fWindow;
	private String id;
	private String[] labels;
}
