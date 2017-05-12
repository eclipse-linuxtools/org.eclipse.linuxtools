/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - Alexander Kurtakov
 *******************************************************************************/
package org.eclipse.linuxtools.internal.systemtap.consolelog;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IHyperlink;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

public class FileHyperlink implements IHyperlink {

	private IFileStore file;
	private int line;

	public FileHyperlink(IFileStore file, int line) {
		this.file = file;
		this.line = line;
	}

	@Override
	public void linkEntered() {
	}

	@Override
	public void linkExited() {
	}

	@Override
	public void linkActivated() {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

		IEditorPart editorPart = null;
		try {
			editorPart = IDE.openEditorOnFileStore(page, file);
			if (line > 0) {
				ITextEditor textEditor = null;
				if (editorPart instanceof ITextEditor) {
					textEditor = (ITextEditor) editorPart;
				} else {
					textEditor = editorPart.getAdapter(ITextEditor.class);
				}
				if (textEditor != null) {
					IEditorInput input = editorPart.getEditorInput();
					IDocumentProvider provider = textEditor.getDocumentProvider();
					try {
						provider.connect(input);
					} catch (CoreException e) {
						// unable to link
						return;
					}
					IDocument document = provider.getDocument(input);
					int offset = -1;
					int length = -1;
					try {
						IRegion region = document.getLineInformation(line - 1);
						offset = region.getOffset();
						length = region.getLength();
					} catch (BadLocationException e) {
						// unable to link
					}
					provider.disconnect(input);
					if (offset >= 0 && length >= 0) {
						textEditor.selectAndReveal(offset, length);
					}
				}
			}
		} catch (PartInitException e) {
			// Put your exception handler here if you wish to
		}

	}

}
