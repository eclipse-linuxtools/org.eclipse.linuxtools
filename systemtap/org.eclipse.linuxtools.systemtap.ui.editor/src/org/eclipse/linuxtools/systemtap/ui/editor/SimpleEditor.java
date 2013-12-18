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

package org.eclipse.linuxtools.systemtap.ui.editor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.linuxtools.internal.systemtap.ui.editor.Localization;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;

public class SimpleEditor extends TextEditor {
	public SimpleEditor() {
		super();
		// make sure we inherit all the text editing commands (delete line etc).
		setKeyBindingScopes(new String[] { "org.eclipse.linuxtools.systemtap.ui.ide.context" }); //$NON-NLS-1$
		internal_init();
	}

	protected void internal_init() {
		configureInsertMode(SMART_INSERT, false);
		setDocumentProvider(new SimpleDocumentProvider());
	}

	@Override
	public void init(final IEditorSite site, final IEditorInput input) throws PartInitException {
		super.init(site, input);
	}

	/**
	 * Searches the IDocument for the specified string.
	 *
	 * @param search string to find
	 * @return the integer line number of the string
	 */
	public int find(String search) {
		IDocument doc = getSourceViewer().getDocument();
		FindReplaceDocumentAdapter finder = new FindReplaceDocumentAdapter(doc);

		int line = 0;

		jumpToLocation(0, 0);
		try {
			IRegion reg = finder.find(0, search, true, false, false, false);
			int offset = reg.getOffset();
			line = doc.getLineOfOffset(offset);
		} catch(BadLocationException ble) {
		} catch(NullPointerException npe) {
			line = -1;
		}

		return line;
	}

	/**
	 * Jumps to the location in the IDocument.
	 * @param line The line you wish to jump to.
	 * @param character The character you wish to jump to.
	 */
	public void jumpToLocation(int line, int character) {
		IDocument doc = getSourceViewer().getDocument();

		try {
			int offset = doc.getLineOffset(line-1) + character;
			this.getSelectionProvider().setSelection(new TextSelection(doc, offset, 0));
		} catch(BadLocationException boe) {}
	}

	/**
	 * Selects a line in the IDocument.
	 * @param line the line you wish to select
	 */
	public void selectLine(int line) {
		IDocument doc = getSourceViewer().getDocument();

		try {
			this.getSelectionProvider().setSelection(new TextSelection(doc, doc.getLineOffset(line-1), doc.getLineLength(line-1)-1));
		} catch(BadLocationException boe) {}
	}

	/**
	 * Performs a SaveAs on the IDocument.
	 */
	@Override
	public void doSaveAs() {
		File file = queryFile();
		if(file == null) {
			return;
		}

		IEditorInput inputFile = createEditorInput(file);

		IDocument doc = getSourceViewer().getDocument();
		String s = doc.get();

		try (FileOutputStream fos = new FileOutputStream(file);
				PrintStream ps = new PrintStream(fos)){
			ps.print(s);
			ps.close();
		} catch(IOException fnfe) {}

		setInput(inputFile);
		setPartName(inputFile.getName());
	}

	/**
	 * Sets up an editor input based on the specified file.
	 * @param file the location of the file you wish to set.
	 * @return input object created.
	 */
	private static IEditorInput createEditorInput(File file) {
		IPath location= new Path(file.getAbsolutePath());
		PathEditorInput input= new PathEditorInput(location);
		return input;
	}

	/**
	 * Inserts text into the IDocument.
	 * @param text string to insert
	 */
	public void insertText(String text) {
		IDocument doc = getSourceViewer().getDocument();
		String s = doc.get();
		int offset = s.length();
		s += text;
		doc.set(s);
		this.setHighlightRange(offset,0,true);
	}

	/**
	 * Inserts text at the current location.
	 * @param text string to insert
	 */
	public void insertTextAtCurrent(String text) {
		ISelection selection = this.getSelectionProvider().getSelection();
		IDocument doc = getSourceViewer().getDocument();

		if(selection instanceof ITextSelection) {
			ITextSelection s = (ITextSelection) selection;
			StringBuffer sb = new StringBuffer(doc.get().substring(0,s.getOffset()));
			sb.append(text.trim());
			sb.append(doc.get().substring(s.getOffset() + s.getLength(), doc.get().length()));
			doc.set(sb.toString());
			this.setHighlightRange(s.getOffset() + text.trim().length(),0,true);
		}
	}

	private static File queryFile() {
		FileDialog dialog= new FileDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.SAVE);
		dialog.setText(Localization.getString("NewFileAction.NewFile"));  //$NON-NLS-1$
		String path= dialog.open();
		if (path != null && path.length() > 0) {
			return new File(path);
		}
		return null;
	}

	/**
	 * Determines whether saving is allowed currently.
	 * @return boolean value indicating whether or not saving is allowed
	 */
	@Override
	public boolean isSaveAsAllowed() {
		return true;
	}

	public static final String ID = "org.eclipse.linuxtools.systemtap.ui.editor.SimpleEditor"; //$NON-NLS-1$
}
