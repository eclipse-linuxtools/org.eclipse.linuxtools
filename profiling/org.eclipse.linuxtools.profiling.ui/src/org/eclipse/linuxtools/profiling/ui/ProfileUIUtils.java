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

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.sourcelookup.ISourceLookupResult;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.linuxtools.internal.profiling.ui.ProfileUIPlugin;
import org.eclipse.ui.IEditorInput;
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
	
	/**
	 * Opens the specified file in an editor (or selects an already open
	 * editor) and highlights the specified line.
	 * @param result - result of performing source lookup with a ISourceLocator
	 * @param line - line number to select, 0 to not select a line
	 * @throws PartInitException - Failed to open editor
	 * @throws BadLocationException - Line number not valid in file
	 * @see DebugUITools#lookupSource(Object, ISourceLocator)
	 */
	public static void openEditorAndSelect(ISourceLookupResult result, int line) throws PartInitException, BadLocationException {
		IEditorInput input = result.getEditorInput();
		String editorID = result.getEditorId();
		
		if (input == null || editorID == null) {
			// Consult the CDT DebugModelPresentation
			Object sourceElement = result.getSourceElement();
			if (sourceElement != null) {				
				IDebugModelPresentation pres = DebugUITools.newDebugModelPresentation(CDebugCorePlugin.getUniqueIdentifier());
				input = pres.getEditorInput(sourceElement);
				editorID = pres.getEditorId(input, sourceElement);
				pres.dispose();
			}
		}
		if (input != null && editorID != null) {
			// Open the editor
			IWorkbenchPage activePage = ProfileUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();;

			IEditorPart editor = IDE.openEditor(activePage, input, editorID);
			// Select the line
			if (editor instanceof ITextEditor) {
				ITextEditor textEditor = (ITextEditor) editor;

				if (line > 0) {
					IDocumentProvider provider = textEditor.getDocumentProvider();
					IDocument document = provider.getDocument(textEditor.getEditorInput());

					IRegion lineRegion = document.getLineInformation(line - 1); //zero-indexed
					textEditor.selectAndReveal(lineRegion.getOffset(), lineRegion.getLength());
				}
			}
		}
	}
	
	/**
	 * Open a file in the Editor at the specified offset, highlighting the given length
	 * 
	 * @param path : Absolute path pointing to the file which will be opened.
	 * @param offset : Offset of the function to be highlighted.
	 * @param length : Length of the function to be highlighted.
	 * @throws PartInitException if the editor could not be initialized
	 */
	public static void openEditorAndSelect(String path, int offset, int length) throws PartInitException {
		Path p = new Path (path);

		if (p.toFile().exists()) {
			IWorkbenchPage activePage = ProfileUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
			IFileStore fileStore = EFS.getLocalFileSystem().getStore(p);

			IEditorPart editor = IDE.openEditorOnFileStore(activePage, fileStore);
			if (editor instanceof ITextEditor) {
				ITextEditor text = (ITextEditor) editor;
				text.selectAndReveal(offset, length);
			}
		}
	}
	
}
