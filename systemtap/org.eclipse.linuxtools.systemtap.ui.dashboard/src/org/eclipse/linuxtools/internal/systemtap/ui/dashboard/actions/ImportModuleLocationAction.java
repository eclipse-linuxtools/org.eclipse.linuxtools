/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse, Anithra P J
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.dashboard.actions;

import java.io.File;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.linuxtools.systemtap.ui.dashboard.internal.DashboardPlugin;
import org.eclipse.linuxtools.systemtap.ui.dashboard.internal.Localization;
import org.eclipse.linuxtools.systemtap.ui.dashboard.preferences.DashboardPreferenceConstants;
import org.eclipse.linuxtools.systemtap.ui.dashboard.views.DashboardModuleBrowserView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * This action allows users to add new directories of DashboardModules.  This provides support for downloading
 * packages from other locations as they become more prevelent.
 * @author Ryan Morse
 */
public class ImportModuleLocationAction extends Action implements IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow fWindow;

	public ImportModuleLocationAction() {
		setEnabled(true);
	}

	@Override
	public void dispose() {
		fWindow= null;
	}

	@Override
	public void init(IWorkbenchWindow window) {
		fWindow= window;
	}

	@Override
	public void run(IAction action) {
		run();
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {}

	/**
	 * This method brings up a dialog window for the user to select the directory
	 * that they have additional modules in.  The new directory is then added
	 * to the list of module directories.  Once added, the dashboard is refreshed to
	 * make sure all modules are included in the view, including those in the folder
	 * just added.
	 */
	@Override
	public void run() {
		File file= queryFolder();
		if (file != null) {
			IPreferenceStore p = DashboardPlugin.getDefault().getPreferenceStore();
			String folders = p.getString(DashboardPreferenceConstants.P_MODULE_FOLDERS);

			p.setValue(DashboardPreferenceConstants.P_MODULE_FOLDERS, folders + File.pathSeparator + file.getAbsolutePath());

			IViewPart ivp = fWindow.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(DashboardModuleBrowserView.ID);
			((DashboardModuleBrowserView)ivp).refresh();
		}
	}

	/**
	 * This method brings up a new dialog window for the user to select the directory
	 * that they have a number of modules in.
	 * @return The folder that the user selected.
	 */
	private File queryFolder() {
		DirectoryDialog dialog= new DirectoryDialog(fWindow.getShell(), SWT.OPEN);
		dialog.setText(Localization.getString("ImportModuleLocationAction.ImportDashboardModules")); //$NON-NLS-1$
		String path= dialog.open();
		if (path != null && path.length() > 0) {
			return new File(path);
		}
		return null;
	}
}
