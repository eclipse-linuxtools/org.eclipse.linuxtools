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
import org.eclipse.linuxtools.rpm.ui.editor.SpecfileEditor;
import org.eclipse.linuxtools.rpm.ui.editor.SpecfileLog;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

public class SpecfileEditorToggleCommentActionDelegate implements
		IEditorActionDelegate {

	SpecfileEditor editor;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IEditorActionDelegate#setActiveEditor(org.eclipse.jface.action.IAction,
	 *      org.eclipse.ui.IEditorPart)
	 */
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		if (targetEditor instanceof SpecfileEditor) {
			editor = (SpecfileEditor) targetEditor;
		}
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
			String selectedContent = "";
			try {
				int begin = document.getLineOffset(selection.getStartLine());
				StringBuffer sb = new StringBuffer(document.get().substring(0,
						begin));
				String content = document.get().substring(begin,
						selection.getOffset() + selection.getLength());
				if (linesContentCommentChar(content)) {
					if (selection.getStartLine() == selection.getEndLine()) {
						selectedContent = "#" + content;
					} else
						selectedContent = "#" + content.replaceAll("\n", "\n#");
				} else {
					selectedContent = content.replaceFirst("#", "").replaceAll(
							"\n#", "\n");
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
	 *      org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
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
				if (line.startsWith("#"))
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

}
