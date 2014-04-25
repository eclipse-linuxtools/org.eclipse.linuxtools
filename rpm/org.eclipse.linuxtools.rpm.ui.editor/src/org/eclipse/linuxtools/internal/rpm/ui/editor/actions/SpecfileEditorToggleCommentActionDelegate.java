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

package org.eclipse.linuxtools.internal.rpm.ui.editor.actions;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.linuxtools.internal.rpm.ui.editor.ISpecfileSpecialSymbols;
import org.eclipse.linuxtools.internal.rpm.ui.editor.SpecfileLog;
import org.eclipse.linuxtools.rpm.ui.editor.SpecfileEditor;
import org.eclipse.ui.handlers.HandlerUtil;

public class SpecfileEditorToggleCommentActionDelegate extends AbstractHandler {

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        SpecfileEditor editor = (SpecfileEditor) HandlerUtil.getActiveEditor(event);
        IDocument document = (IDocument) editor.getAdapter(IDocument.class);
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
                        selectedContent = ISpecfileSpecialSymbols.COMMENT_START + content;
                    } else {
                        selectedContent = ISpecfileSpecialSymbols.COMMENT_START + content.replaceAll("\n", "\n#"); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                } else {
                    selectedContent = content.replaceFirst(ISpecfileSpecialSymbols.COMMENT_START, "").replaceAll( //$NON-NLS-1$
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
        return null;
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
