/*******************************************************************************
 * Copyright (c) 2007 Alphonse Van Assche.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alphonse Van Assche - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.rpm.ui.editor.actions;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.linuxtools.rpm.ui.editor.Activator;
import org.eclipse.linuxtools.rpm.ui.editor.SpecfileEditor;
import org.eclipse.linuxtools.rpm.ui.editor.SpecfileLog;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public class SpecfileEditorToggleCommentActionDelegate implements
		IEditorActionDelegate, IWorkbenchWindowActionDelegate {

	SpecfileEditor editor;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
	 *      org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		if (Activator.getActiveEditor() instanceof SpecfileEditor)
			editor = (SpecfileEditor) Activator.getActiveEditor();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		IDocument document = editor.getSpecfileSourceViewer().getDocument();
		ISelection currentSelection = editor.getSpecfileSourceViewer()
				.getSelection();
		if (currentSelection instanceof ITextSelection) {
			ITextSelection selection = (ITextSelection) currentSelection;
			String selectedContent = ""; //$NON-NLS-1$
			try {
				int begin = document.getLineOffset(selection.getStartLine());
				StringBuilder sb = new StringBuilder(document.get().substring(0,
						begin));
				String content = document.get().substring(begin,
						selection.getOffset() + selection.getLength());
				if (linesContentCommentChar(content)) {
					if (selection.getStartLine() == selection.getEndLine()) {
						selectedContent = "#" + content; //$NON-NLS-1$
					} else
						selectedContent = "#" + content.replaceAll("\n", "\n#"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				} else {
					selectedContent = content.replaceFirst("#", "").replaceAll( //$NON-NLS-1$ //$NON-NLS-2$
							"\n#", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
				}
				sb.append(selectedContent);
				sb.append(document.get().substring(
						selection.getOffset() + selection.getLength(),
						document.get().length()));
				document.set(sb.toString());
				editor.setHighlightRange(selection.getOffset(), selection
						.getLength(), true);
			} catch (BadLocationException e) {
				SpecfileLog.logError(e);
			}
		}
	}

	/**
	 * Check if all lines are commented
	 * 
	 * @param content
	 *            to check
	 * @return true if all lines begin with '#' char
	 */
	private boolean linesContentCommentChar(String content) {
		LineNumberReader reader = new LineNumberReader(
				new StringReader(content));
		String line;
		boolean ret = false;
		try {
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("#")) //$NON-NLS-1$
					ret = false;
				else
					return true;
			}
		} catch (IOException e) {
			SpecfileLog.logError(e);
			return false;
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IEditorActionDelegate#setActiveEditor(org.eclipse.jface.action.IAction,
	 *      org.eclipse.ui.IEditorPart)
	 */
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {

	}
}
