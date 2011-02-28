/*******************************************************************************
 * Copyright (c) 2008 Alexander Kurtakov.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Kurtakov - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.ui.editor.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.linuxtools.rpm.ui.editor.Activator;
import org.eclipse.linuxtools.rpm.ui.editor.SpecfileEditor;
import org.eclipse.linuxtools.rpm.ui.editor.outline.SpecfileQuickOutlineDialog;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public class SpecfileEditorShowOutlineActionDelegate implements
		IEditorActionDelegate, IWorkbenchWindowActionDelegate {

	SpecfileEditor editor;

	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		if (targetEditor instanceof SpecfileEditor) {
			editor = (SpecfileEditor) targetEditor;
		} else if (Activator.getActiveEditor() instanceof SpecfileEditor) {
			editor = (SpecfileEditor) Activator.getActiveEditor();
		}
	}

	public void run(IAction action) {
		if (Activator.getActiveEditor() instanceof SpecfileEditor){
			editor = (SpecfileEditor) Activator.getActiveEditor();
		}
		SpecfileQuickOutlineDialog quickOutlinePopupDialog = new SpecfileQuickOutlineDialog(
				editor.getSite().getShell(), SWT.NONE, editor);
		quickOutlinePopupDialog.setSize(400, 200);
		quickOutlinePopupDialog.setVisible(true);
	}

	public void selectionChanged(IAction action, ISelection selection) {
		if (Activator.getActiveEditor() instanceof SpecfileEditor)
			editor = (SpecfileEditor) Activator.getActiveEditor();
	}

	public void dispose() {
	}

	public void init(IWorkbenchWindow window) {
	}

}
