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

package org.eclipse.linuxtools.internal.systemtap.ui.ide.actions;

import java.io.File;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.IDEPlugin;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.preferences.IDEPreferenceConstants;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.views.FunctionBrowserView;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.views.ProbeAliasBrowserView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;


/**
 * This <code>Action</code> is used when the user modifies the list of tapset locations.
 * When it is invoked, the user is presented with a file selection dialog prompting them to
 * select a location. If the user confirms their selection in this dialog (through the native
 * widget's method, usually an OK box), the path the user selected is added to the list stored
 * in preferences that contains tapset locations, and the tapset browsers are told to refresh.
 * @author Ryan Morse
 * @see org.eclipse.linuxtools.internal.systemtap.ui.ide.views.TapsetBrowserView
 */
public class ImportTapsetAction extends Action implements IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow fWindow;

	public ImportTapsetAction() {
		setEnabled(true);
	}

	@Override
	public void dispose() {
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
	public void selectionChanged(IAction action, ISelection selection) {
	}

	/**
	 * This method displays the dialog box to the user that prompts for a tapset location.
	 * @return	A File representing the directory that the user selected, if the user confirmed, or null otherwise.
	 */
	private File queryFile() {
		DirectoryDialog dialog= new DirectoryDialog(fWindow.getShell(), SWT.OPEN);
		dialog.setText("Import Tapsets"); //$NON-NLS-1$
		String path= dialog.open();
		if (path != null && path.length() > 0) {
			return new File(path);
		}
		return null;
	}

	/**
	 * The main body of this event. Prompts the user for a new location using the <code>queryFile</code>
	 * method, then if the return from <code>queryFile</code> is non-null, the path is added to preferences
	 * and the tapset browsers refreshed.
	 */
	@Override
	public void run() {
		File file= queryFile();
		if (file != null) {
			IPreferenceStore p = IDEPlugin.getDefault().getPreferenceStore();
			String tapsets = p.getString(IDEPreferenceConstants.P_TAPSETS);

			p.setValue(IDEPreferenceConstants.P_TAPSETS, tapsets + File.pathSeparator + file.getAbsolutePath());

			IViewPart ivp = fWindow.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(FunctionBrowserView.ID);
			((FunctionBrowserView)ivp).refresh();
			ivp = fWindow.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(ProbeAliasBrowserView.ID);
			((ProbeAliasBrowserView)ivp).refresh();
		}
	}
}
