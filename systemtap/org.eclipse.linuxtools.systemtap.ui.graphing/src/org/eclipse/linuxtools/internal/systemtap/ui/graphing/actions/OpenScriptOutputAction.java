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
import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.IDataEntry;
import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.IDataSet;
import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.IDataSetParser;
import org.eclipse.linuxtools.systemtap.graphingapi.ui.widgets.ExceptionErrorDialog;
import org.eclipse.linuxtools.systemtap.graphingapi.ui.wizards.dataset.DataSetWizard;
import org.eclipse.linuxtools.systemtap.ui.graphing.GraphingConstants;
import org.eclipse.linuxtools.systemtap.ui.graphing.views.GraphSelectorEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;



/**
 * This action is designed to open up the raw script output from a stap command.
 * It will act just like the user is running a script with graphing, but will
 * not cause update events.
 * @author Ryan Morse
 */
public class OpenScriptOutputAction extends Action implements IWorkbenchWindowActionDelegate {
	@Override
	public void init(IWorkbenchWindow window) {
		fWindow = window;
	}

	/**
	 * This is the main method of the action.  It handles prompting the user
	 * for a file that they want to graph.  Then prompts the user to select a
	 * parsing expression to use to break the table into tabular output.  Finally,
	 * it will generate a new <code>DataSet</code> to hold all of the data.
	 * @param act The action that fired this method.
	 */
	@Override
	public void run(IAction act) {
		File f = queryFile();

		if (f == null){
			return;
		}

		if(!f.exists()) {
			displayError(Localization.getString("OpenScriptOutputAction.SelectedFileDNE")); //$NON-NLS-1$
		} else if(!f.canRead()) {
			displayError(Localization.getString("OpenScriptOutputAction.SelectedFileCanNotRead")); //$NON-NLS-1$
		} else {
			//Get the file from the user
			StringBuilder sb = readFile(f);
			if(getChartingOptions(f.getAbsolutePath())) {
				IDataEntry output;
				while(true) {
					output = parser.parse(sb);
					if(null != output) {
						dataSet.setData(output);
					} else {
						break;
					}
				}

				IViewPart ivp = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(GraphSelectorEditor.ID);
				((GraphSelectorEditor)ivp).createScriptSet(f.getName(), dataSet);
			}
		}
	}


	/**
	 * This method will display a dialog box for the user to select a
	 * location to open a file from.
	 * @return The File selected to open.
	 */
	private File queryFile() {
		FileDialog dialog= new FileDialog(fWindow.getShell(), SWT.OPEN);
		dialog.setText(Localization.getString("OpenScriptOutputAction.OpenFile")); //$NON-NLS-1$
		String path= dialog.open();
		if (path != null && path.length() > 0) {
			return new File(path);
		}
		return null;
	}

	/**
	 * This method will display the error message to the user in the case
	 * that something went wrong.
	 * @param message The message that should be shown in the error dialog.
	 */
	private void displayError(String message) {
		MessageDialog.openWarning(fWindow.getShell(), Localization.getString("OpenScriptOutputAction.Problem"), message); //$NON-NLS-1$
	}

	/**
	 * This method will read the contents of the provided file and
	 * add the contents to the provided StringBuilder.
	 * @param f The file that will be opened for reading
	 * @param The contents of the file
	 */
	private StringBuilder readFile(File f) {
		StringBuilder sb = new StringBuilder();
		try {
			FileReader fr = new FileReader(f);
			BufferedReader br = new BufferedReader(fr);

			String line;
			while(null != (line=br.readLine())) {
				sb.append(line);
			}
			br.close();
		} catch(FileNotFoundException fnfe) {
			ExceptionErrorDialog.openError(Localization.getString("OpenScriptOutputAction.ErrorReadingFile"), fnfe); //$NON-NLS-1$
		} catch(IOException ioe) {
			ExceptionErrorDialog.openError(Localization.getString("OpenScriptOutputAction.ErrorReadingFile"), ioe); //$NON-NLS-1$
		}
		return sb;
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

	@Override
	public void selectionChanged(IAction a, ISelection s) {
	}

	/**
	 * Removes all internal references in this class.  Nothing should make any references
	 * to anyting in this class after calling the dispose method.
	 */
	@Override
	public void dispose() {
		fWindow = null;
		parser = null;
	}

	private IWorkbenchWindow fWindow;
	private IDataSet dataSet;
	private IDataSetParser parser;
}
