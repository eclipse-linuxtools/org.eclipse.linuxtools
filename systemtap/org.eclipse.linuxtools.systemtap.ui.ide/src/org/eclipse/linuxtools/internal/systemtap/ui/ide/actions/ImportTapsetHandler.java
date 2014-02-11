/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse: initial API
 *     Red Hat - Andrew Ferrazzutti, Alex Kurtakov: conversion from Action to Handler
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide.actions;

import java.io.File;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.IDEPlugin;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.preferences.IDEPreferenceConstants;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.views.FunctionBrowserView;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.views.ProbeAliasBrowserView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;

public class ImportTapsetHandler extends AbstractHandler {

	/**
	 * This method displays the dialog box to the user that prompts for a tapset location.
	 * @return	A File representing the directory that the user selected, if the user confirmed, or null otherwise.
	 */
	private File queryFile(Shell shell) {
		DirectoryDialog dialog= new DirectoryDialog(shell, SWT.OPEN);
		dialog.setText("Import Tapsets"); //$NON-NLS-1$
		String path= dialog.open();
		if (path != null && !path.isEmpty()) {
			return new File(path);
		}
		return null;
	}

	@Override
	public Object execute(ExecutionEvent event) {
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		File file = queryFile(shell);
		if (file != null) {
			IPreferenceStore p = IDEPlugin.getDefault().getPreferenceStore();
			String tapsets = p.getString(IDEPreferenceConstants.P_TAPSETS);

			p.setValue(IDEPreferenceConstants.P_TAPSETS, tapsets + File.pathSeparator + file.getAbsolutePath());

			IViewPart ivp = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(FunctionBrowserView.ID);
			((FunctionBrowserView)ivp).refresh();
			ivp = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(ProbeAliasBrowserView.ID);
			((ProbeAliasBrowserView)ivp).refresh();
		}
		return null;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

}
