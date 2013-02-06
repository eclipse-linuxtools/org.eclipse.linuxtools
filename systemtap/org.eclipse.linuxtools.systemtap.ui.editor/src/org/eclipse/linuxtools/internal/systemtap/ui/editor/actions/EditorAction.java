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

package org.eclipse.linuxtools.internal.systemtap.ui.editor.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

public abstract class EditorAction extends Action implements IWorkbenchWindowActionDelegate {
	public EditorAction() {
		super();
		setEnabled(true);
	}

	@Override
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	protected void updateState() {
		IEditorPart editor = getActiveEditor();
		setEnabled(editor != null && editor.isDirty());
	}

	protected IWorkbenchWindow getWorkbenchWindow() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	}

	protected IWorkbenchPage getActivePage() {
		return getWorkbenchWindow().getActivePage();
	}

	protected IEditorPart getActiveEditor() {
		return getActivePage().getActiveEditor();
	}

	@Override
	public void selectionChanged(IAction act, ISelection select) {
		action = act;
		buildEnablementChecks();
	}
	
	protected void buildEnablementChecks() {
		setEnablement(true);
	}
	
	protected void setEnablement(boolean enabled) {
		action.setEnabled(enabled);
	}

	@Override
	public void dispose() {
		window = null;
		action = null;
	}
	
	protected IWorkbenchWindow window;
	protected IAction action;
}
