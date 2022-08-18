/*******************************************************************************
 * Copyright (c) 2007, 2018 Alphonse Van Assche and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Alphonse Van Assche - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.rpm.ui.editor.actions;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.linuxtools.internal.rpm.ui.editor.ISpecfileSpecialSymbols;
import org.eclipse.linuxtools.internal.rpm.ui.editor.SpecfileLog;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

public class SpecfileEditorToggleCommentActionDelegate extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) {
		IEditorPart editor = HandlerUtil.getActiveEditor(event);
		ITextEditor specfileEditor = editor.getAdapter(ITextEditor.class);
		IDocumentProvider dp = specfileEditor.getDocumentProvider();
		IDocument document = dp.getDocument(specfileEditor.getEditorInput());
		ISelection currentSelection = HandlerUtil.getCurrentSelection(event);
		if (currentSelection instanceof ITextSelection selection) {
			String selectedContent = ""; //$NON-NLS-1$
			try {
				int begin = document.getLineOffset(selection.getStartLine());
				StringBuilder sb = new StringBuilder(document.get().substring(0, begin));
				String content = document.get().substring(begin, selection.getOffset() + selection.getLength());
				if (linesContentCommentChar(content)) {
					if (selection.getStartLine() == selection.getEndLine()) {
						selectedContent = ISpecfileSpecialSymbols.COMMENT_START + content;
					} else {
						selectedContent = ISpecfileSpecialSymbols.COMMENT_START + content.replace("\n", "\n#"); //$NON-NLS-1$ //$NON-NLS-2$
					}
				} else {
					selectedContent = content.replaceFirst(ISpecfileSpecialSymbols.COMMENT_START, "").replace( //$NON-NLS-1$
							"\n#", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
				}
				sb.append(selectedContent);
				sb.append(document.get().substring(selection.getOffset() + selection.getLength(),
						document.get().length()));
				document.set(sb.toString());
				specfileEditor.setHighlightRange(selection.getOffset(), selection.getLength(), true);
			} catch (BadLocationException e) {
				SpecfileLog.logError(e);
			}
		}
		return null;
	}

	/**
	 * Check if all lines are commented
	 *
	 * @param content to check
	 * @return true if all lines begin with '#' char
	 */
	private boolean linesContentCommentChar(String content) {
		LineNumberReader reader = new LineNumberReader(new StringReader(content));
		String line;
		boolean ret = false;
		try {
			while ((line = reader.readLine()) != null) {
				if (line.startsWith(ISpecfileSpecialSymbols.COMMENT_START)) {
					ret = false;
				} else {
					return true;
				}
			}
		} catch (IOException e) {
			SpecfileLog.logError(e);
			return false;
		}
		return ret;
	}

}
