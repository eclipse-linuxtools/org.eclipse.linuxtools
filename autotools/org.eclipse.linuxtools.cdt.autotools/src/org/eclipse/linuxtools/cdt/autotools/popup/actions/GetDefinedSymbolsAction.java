/*******************************************************************************
 * Copyright (c) 2006 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.cdt.autotools.popup.actions;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.linuxtools.cdt.autotools.AutotoolsScannerInfo;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;


// TODO: This is just a tester of fetching the include path
// at build.  It is meant to eventually end up being
// called by the indexer to remove the need for preset
// includePaths.
public class GetDefinedSymbolsAction implements IObjectActionDelegate {
	
	IFile file;

	/**
	 * Constructor for Action1.
	 */
	public GetDefinedSymbolsAction() {
		super();
		file = null;
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	@SuppressWarnings("unchecked")
	public void run(IAction action) {
		Map p = new HashMap();
		if (file != null) {
			AutotoolsScannerInfo amfm = new AutotoolsScannerInfo(file);
			p = amfm.getDefinedSymbols();
		}
		Shell shell = new Shell();
		MessageDialog.openInformation(
			shell,
			"Autoconf Plug-in Defined Symbols for " + file.getFullPath().toOSString(),
			p.toString());
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
        if (selection instanceof IStructuredSelection) {
        	IStructuredSelection s = (IStructuredSelection)selection;
        	this.file = (IFile)s.getFirstElement();
        }
	}
}
