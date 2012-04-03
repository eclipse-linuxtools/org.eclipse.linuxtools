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

import java.io.File;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.linuxtools.internal.systemtap.ui.graphing.Localization;
import org.eclipse.linuxtools.systemtap.ui.graphing.structures.GraphDisplaySet;
import org.eclipse.linuxtools.systemtap.ui.graphing.views.GraphSelectorView;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.datasets.IDataSet;
import org.eclipse.linuxtools.systemtap.ui.logging.LogManager;
import org.eclipse.linuxtools.systemtap.ui.structures.listeners.ITabListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;



/**
 * This action handles exporting all of the data that was collected for the DataSet.  It
 * exports everything as a table, that can easily be read back in at a later time.
 * @author Ryan Morse
 */
public class ExportDataSetAction extends Action implements IWorkbenchWindowActionDelegate {
	public void init(IWorkbenchWindow window) {
		LogManager.logDebug("Start ExportDataSetAction.init", this);
		LogManager.logInfo("Initialize ExportDataSetAction", this);
		fWindow = window;
		LogManager.logDebug("End ExportDataSetAction.init", this);
	}

	/**
	 * This is the main method of the action.  It handles getting the active dataset,
	 * and then saving it to a file that can be accessed later.
	 * @param act The action that fired this method.
	 */
	public void run(IAction act) {
		LogManager.logDebug("Start ExportDataSetAction.run", this);
		File f = null;
		IDataSet data = getDataSet();

		if(null != data)
			f = getFile();

		if(f != null && data != null)
			data.writeToFile(f);
		LogManager.logDebug("End ExportDataSetAction.run", this);
	}
	
	/**
	 * This method retreives the active <code>DataSet</code> from the <code>GraphSelectorView</code>.  If no
	 * DataSet is active it will return null.
	 * @return The IDataSet in tha active display set.
	 */
	public IDataSet getDataSet() {
		LogManager.logDebug("Start ExportDataSetAction.getDataSet", this);
		IViewPart ivp = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(GraphSelectorView.ID);
		IDataSet data = null;
		GraphDisplaySet gds = ((GraphSelectorView)ivp).getActiveDisplaySet();
		if(null != gds)
			data = gds.getDataSet();
		LogManager.logDebug("End ExportDataSetAction.getDataSet", this);
		return data;
	}
	
	/**
	 * This method will display a dialog box for the user to select a 
	 * location to save the graph image.
	 * @return The File selected to save the image to.
	 */
	public File getFile() {
		LogManager.logDebug("Start ExportDataSetAction.getFile", this);
		String path = null;
		FileDialog dialog= new FileDialog(fWindow.getShell(), SWT.SAVE);
		dialog.setText(Localization.getString("ExportDataSetAction.NewFile"));

		path = dialog.open();
		
		if(null == path)
			return null;

		LogManager.logDebug("End ExportDataSetAction.getFile", this);
		return new File(path);
	}
	
	public void selectionChanged(IAction a, ISelection s) {
		action = a;
		setEnablement(false);
		buildEnablementChecks();
	}
	
	/**
	 * This method is used to generate the checks to see it this button 
	 * should be enabled or not.
	 */
	private void buildEnablementChecks() {
		IViewPart ivp = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(GraphSelectorView.ID);
		if(null != ivp) {
			final GraphSelectorView gsv = (GraphSelectorView)ivp;
			setEnablement(null != gsv.getActiveDisplaySet());
			gsv.addTabListener(new ITabListener() {
				public void tabClosed() {
					setEnablement(null != gsv.getActiveDisplaySet());
				}
				
				public void tabOpened() {
					setEnablement(true);
				}
				
				public void tabChanged() {}
			});
		}
	}
	
	private void setEnablement(boolean enable) {
		action.setEnabled(enable);
	}
	
	/**
	 * Removes all internal references in this class.  Nothing should make any references
	 * to anyting in this class after calling the dispose method.
	 */
	public void dispose() {
		LogManager.logDebug("Start ExportDataSetAction.dispose", this);
		LogManager.logInfo("Dispose ExportDataSetAction", this);
		fWindow = null;
		action = null;
		LogManager.logDebug("End ExportDataSetAction.dispose", this);
	}
	
	private IWorkbenchWindow fWindow;
	private IAction action;
}
