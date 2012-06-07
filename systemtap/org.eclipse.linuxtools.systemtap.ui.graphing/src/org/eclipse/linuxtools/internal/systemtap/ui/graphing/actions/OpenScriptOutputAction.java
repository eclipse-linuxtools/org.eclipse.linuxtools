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
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.linuxtools.internal.systemtap.ui.graphing.Localization;
import org.eclipse.linuxtools.systemtap.ui.graphing.GraphingConstants;
import org.eclipse.linuxtools.systemtap.ui.graphing.GraphingPerspective;
import org.eclipse.linuxtools.systemtap.ui.graphing.views.GraphSelectorView;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.datasets.IDataEntry;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.datasets.IDataSet;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.datasets.IDataSetParser;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.ui.wizards.dataset.DataSetWizard;
import org.eclipse.linuxtools.systemtap.ui.logging.LogManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;



/**
 * This action is designed to open up the raw script output from a stap command.
 * It will act just like the user is running a script with graphing, but will
 * not cause update events.
 * @author Ryan Morse
 */
public class OpenScriptOutputAction extends Action implements IWorkbenchWindowActionDelegate {
	public void init(IWorkbenchWindow window) {
		LogManager.logDebug("Start OpenScriptOutputAction.init", this); //$NON-NLS-1$
		LogManager.logDebug("Initializing", this); //$NON-NLS-1$
		fWindow = window;
		LogManager.logDebug("End OpenScriptOutputAction.init", this); //$NON-NLS-1$
	}

	/**
	 * This is the main method of the action.  It handles prompting the user
	 * for a file that they want to graph.  Then prompts the user to select a
	 * parsing expression to use to break the table into tabular output.  Finally,
	 * it will generate a new <code>DataSet</code> to hold all of the data.
	 * @param act The action that fired this method.
	 */
	public void run(IAction act) {
		LogManager.logDebug("Start OpenScriptOutputAction.run", this); //$NON-NLS-1$
		
		File f = queryFile();
		
		if(null == f) {
		} else if(!f.exists()) {
			displayError(Localization.getString("OpenScriptOutputAction.SelectedFileDNE"));
		} else if(!f.canRead()) {
			displayError(Localization.getString("OpenScriptOutputAction.SelectedFileCanNotRead"));
		} else {
			//Get the file from the user
			StringBuilder sb = new StringBuilder();
			readFile(f, sb);
			if(getChartingOptions(f.getAbsolutePath())) {
				IDataEntry output;
				while(true) {
					output = parser.parse(sb);
					if(null != output)
						dataSet.setData(output);
					else
						break;
				}

				try {
					IWorkbenchPage p = PlatformUI.getWorkbench().showPerspective(GraphingPerspective.ID, PlatformUI.getWorkbench().getActiveWorkbenchWindow());
					IViewPart ivp = p.findView(GraphSelectorView.ID);
					((GraphSelectorView)ivp).createScriptSet(f.getName(), dataSet);
				} catch(WorkbenchException we) {
					LogManager.logCritical("WorkbenchException OpenScriptOutputAction.run:" + we.getMessage(), this); //$NON-NLS-1$
				}
			}
		}

		LogManager.logDebug("End OpenScriptOutputAction.run", this); //$NON-NLS-1$
	}
	

	/**
	 * This method will display a dialog box for the user to select a 
	 * location to open a file from.
	 * @return The File selected to open.
	 */
	private File queryFile() {
		LogManager.logDebug("Start queryFile:", this); //$NON-NLS-1$
		FileDialog dialog= new FileDialog(fWindow.getShell(), SWT.OPEN);
		dialog.setText(Localization.getString("OpenScriptOutputAction.OpenFile"));
		String path= dialog.open();
		if (path != null && path.length() > 0) {
			LogManager.logDebug("queryFile: returnVal-" + path, this); //$NON-NLS-1$
			return new File(path);
		}
		LogManager.logDebug("queryFile: returnVal-null", this); //$NON-NLS-1$
		return null;
	}

	/**
	 * This method will display the error message to the user in the case
	 * that something went wrong.
	 * @param message The message that should be shown in the error dialog.
	 */
	private void displayError(String message) {
		LogManager.logDebug("Start OpenScriptOutputAction.displayError", this); //$NON-NLS-1$
		LogManager.logInfo("Initializing", MessageDialog.class); //$NON-NLS-1$
		MessageDialog.openWarning(fWindow.getShell(), Localization.getString("OpenScriptOutputAction.Problem"), message);
		LogManager.logInfo("Disposing", MessageDialog.class); //$NON-NLS-1$
		LogManager.logDebug("End OpenScriptOutputAction.displayError", this); //$NON-NLS-1$
	}
	
	/**
	 * This method will read the contents of the provided file and
	 * add the contents to the provided StringBuilder.
	 * @param f The file that will be opened for reading
	 * @param sb The StringBuilder to store the contents of the file
	 */
	private void readFile(File f, StringBuilder sb) {
		LogManager.logDebug("Start ImportDataSetAction.readData", this); //$NON-NLS-1$
		try {
			FileReader fr = new FileReader(f);
			BufferedReader br = new BufferedReader(fr);
			
			String line;
			while(null != (line=br.readLine())) {
				sb.append(line);
			}
			br.close();
		} catch(FileNotFoundException fnfe) {
			fnfe.printStackTrace();
			LogManager.logCritical("FileNotFoundException ImportDataSetAction.readData:" + fnfe.getMessage(), this); //$NON-NLS-1$
		} catch(IOException ioe) {
			ioe.printStackTrace();
			LogManager.logCritical("IOException ImportDataSetAction.readData:" + ioe.getMessage(), this); //$NON-NLS-1$
		}
		LogManager.logDebug("End ImportDataSetAction.readData", this); //$NON-NLS-1$
	}
	
	/**
	 * This method will get all of the parsing information from the user.
	 * @param filePath The location of the file to be opened.
	 * @return boolean representing whether or not it was successful
	 */
	protected boolean getChartingOptions(String filePath) {
		DataSetWizard wizard = new DataSetWizard(GraphingConstants.DataSetMetaData, filePath);
		IWorkbench workbench = PlatformUI.getWorkbench();
		wizard.init(workbench, null);
		WizardDialog dialog = new WizardDialog(workbench.getActiveWorkbenchWindow().getShell(), wizard);
		dialog.create();
		dialog.open();

		parser = wizard.getParser();
		dataSet = wizard.getDataSet();

		wizard.dispose();
		return(null != parser && null != dataSet);
	}
	
	public void selectionChanged(IAction a, ISelection s) {
	}
	
	/**
	 * Removes all internal references in this class.  Nothing should make any references
	 * to anyting in this class after calling the dispose method.
	 */
	public void dispose() {
		LogManager.logDebug("Start OpenScriptOutputAction.dispose", this); //$NON-NLS-1$
		LogManager.logInfo("Disposing", this); //$NON-NLS-1$
		fWindow = null;
		parser = null;
		LogManager.logDebug("End OpenScriptOutputAction.dispose", this); //$NON-NLS-1$
	}
	
	private IWorkbenchWindow fWindow;
	private IDataSet dataSet;
	private IDataSetParser parser;
}
