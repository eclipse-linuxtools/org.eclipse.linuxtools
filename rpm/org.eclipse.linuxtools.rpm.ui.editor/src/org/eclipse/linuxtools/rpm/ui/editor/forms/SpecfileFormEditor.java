/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.ui.editor.forms;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.linuxtools.rpm.ui.editor.SpecfileEditor;
import org.eclipse.linuxtools.rpm.ui.editor.parser.Specfile;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileParser;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class SpecfileFormEditor extends FormEditor {

	FormPage mainPackage;
	SpecfileEditor editor;
	private Specfile specfile;
	SpecfileParser parser;

	public SpecfileFormEditor() {
		editor = new SpecfileEditor();
	}

	@Override
	protected FormToolkit createToolkit(Display display) {
		// Create a toolkit that shares colors between editors.
		return new FormToolkit(Display.getCurrent());
	}

	@Override
	protected void addPages() {
		try {
			int index = addPage(editor, getEditorInput());
			setPageText(index, Messages.SpecfileFormEditor_0);
			specfile = editor.getSpecfile();
			mainPackage = new MainPackagePage(this, specfile);
			addPage(0, mainPackage);
		} catch (PartInitException e) {
			//
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		editor.doSave(monitor);
	}

	@Override
	public void doSaveAs() {
		//noop
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public boolean isDirty() {
		return editor.isDirty();
	}
}
