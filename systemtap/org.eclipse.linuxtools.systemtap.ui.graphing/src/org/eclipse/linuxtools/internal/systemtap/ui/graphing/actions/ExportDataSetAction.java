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
import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.IDataSet;
import org.eclipse.linuxtools.systemtap.structures.listeners.ITabListener;
import org.eclipse.linuxtools.systemtap.ui.graphing.GraphDisplaySet;
import org.eclipse.linuxtools.systemtap.ui.graphing.views.GraphSelectorEditor;
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
	@Override
	public void init(IWorkbenchWindow window) {
		fWindow = window;
	}

	/**
	 * This is the main method of the action.  It handles getting the active dataset,
	 * and then saving it to a file that can be accessed later.
	 * @param act The action that fired this method.
	 */
	@Override
	public void run(IAction act) {
		File f = null;
		IDataSet data = getDataSet();

		if(null != data) {
			f = getFile();
		}

		if(f != null && data != null) {
			data.writeToFile(f);
		}
	}

	/**
	 * This method retreives the active <code>DataSet</code> from the <code>GraphSelectorView</code>.  If no
	 * DataSet is active it will return null.
	 * @return The IDataSet in tha active display set.
	 */
	public IDataSet getDataSet() {
		IViewPart ivp = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(GraphSelectorEditor.ID);
		IDataSet data = null;
		GraphDisplaySet gds = ((GraphSelectorEditor)ivp).getActiveDisplaySet();
		if(null != gds) {
			data = gds.getDataSet();
		}
		return data;
	}

	/**
	 * This method will display a dialog box for the user to select a
	 * location to save the graph image.
	 * @return The File selected to save the image to.
	 */
	public File getFile() {
		String path = null;
		FileDialog dialog= new FileDialog(fWindow.getShell(), SWT.SAVE);
		dialog.setText(Localization.getString("ExportDataSetAction.NewFile")); //$NON-NLS-1$

		path = dialog.open();

		if(null == path) {
			return null;
		}

		return new File(path);
	}

	@Override
	public void selectionChanged(IAction a, ISelection s) {
		action = a;
		action.setEnabled(false);
		buildEnablementChecks();
	}

	/**
	 * This method is used to generate the checks to see it this button
	 * should be enabled or not.
	 */
	private void buildEnablementChecks() {
		IViewPart ivp = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(GraphSelectorEditor.ID);
		if(null != ivp) {
			final GraphSelectorEditor gsv = (GraphSelectorEditor)ivp;
			action.setEnabled(null != gsv.getActiveDisplaySet());
			gsv.addTabListener(new ITabListener() {
				@Override
				public void tabClosed() {
					action.setEnabled(null != gsv.getActiveDisplaySet());
				}

				@Override
				public void tabOpened() {
					action.setEnabled(true);
				}

				@Override
				public void tabChanged() {}
			});
		}
	}

	/**
	 * Removes all internal references in this class.  Nothing should make any references
	 * to anyting in this class after calling the dispose method.
	 */
	@Override
	public void dispose() {
		fWindow = null;
		action = null;
	}

	private IWorkbenchWindow fWindow;
	private IAction action;
}
