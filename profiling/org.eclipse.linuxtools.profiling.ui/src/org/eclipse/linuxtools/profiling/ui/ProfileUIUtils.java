/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.profiling.ui;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

public class ProfileUIUtils {
	
	/**
	 * Opens the specified file in an editor (or selects an already open
	 * editor) and highlights the specified line.
	 * @param path - absolute path of file to open
	 * @param line - line number to select, 0 to not select a line
	 * @throws PartInitException - Failed to open editor
	 * @throws BadLocationException - Line number not valid in file
	 */
	public static void openEditorAndSelect(String path, int line) throws PartInitException, BadLocationException {
		Path p = new Path(path);

		if (p.toFile().exists()) {
			IWorkbenchPage activePage = ProfileUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
			IFileStore file = EFS.getLocalFileSystem().getStore(p);

			IEditorPart editor = IDE.openEditorOnFileStore(activePage, file);
			if (editor instanceof ITextEditor) {
				ITextEditor textEditor = (ITextEditor) editor;

				if (line > 0) {
					IDocumentProvider provider = textEditor.getDocumentProvider();
					IDocument document = provider.getDocument(textEditor.getEditorInput());

					int start = document.getLineOffset(line - 1); //zero-indexed
					textEditor.selectAndReveal(start, 0);
				}
			}
		}
	}
}
