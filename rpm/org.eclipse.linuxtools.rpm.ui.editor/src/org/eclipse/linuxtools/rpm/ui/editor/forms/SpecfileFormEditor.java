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
	boolean dirty = true;

	@Override
	public boolean isDirty() {
		return dirty;
	}

	public SpecfileFormEditor() {
		editor = new SpecfileEditor();
		parser = new SpecfileParser();
	}

	@Override
	protected FormToolkit createToolkit(Display display) {
		// Create a toolkit that shares colors between editors.
		return new FormToolkit(Display.getCurrent());
	}

	@Override
	protected void addPages() {
		try {
			editor.init(getEditorSite(), getEditorInput());
			editor.setInput(getEditorInput());
			specfile = parser.parse(editor.getDocumentProvider().getDocument(
					getEditorInput()));
			mainPackage = new MainPackagePage(this,specfile);
			addPage(mainPackage);
			int index = addPage(editor, getEditorInput());
			setPageText(index, "Source");
			// setActivePage(index);

		} catch (PartInitException e) {
			//
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		System.out.println("Name:" + specfile.getName());
	}

	@Override
	public void doSaveAs() {

	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}
}
