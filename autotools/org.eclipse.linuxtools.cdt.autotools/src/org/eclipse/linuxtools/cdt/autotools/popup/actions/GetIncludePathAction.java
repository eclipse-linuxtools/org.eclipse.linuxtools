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
public class GetIncludePathAction implements IObjectActionDelegate {
	
	IFile file;

	/**
	 * Constructor for Action1.
	 */
	public GetIncludePathAction() {
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
	public void run(IAction action) {
		StringBuffer sb = new StringBuffer();
		if (file != null) {
			AutotoolsScannerInfo amfm = new AutotoolsScannerInfo(file);
			String[] p = amfm.getIncludePaths();
			for (int i = 0; i < p.length; ++i) {
				sb.append(p[i]);
				sb.append("\n");
			}
		} else {
			sb.append("No path available");
		}
		Shell shell = new Shell();
		MessageDialog.openInformation(
			shell,
			"Autoconf Plug-in Include Path for " + file.getFullPath().toOSString(),
			sb.toString());
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
