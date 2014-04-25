/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.ui.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

public class SuppressionsContentAssistProcessor implements
        IContentAssistProcessor {

    @Override
    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer,
            int offset) {
        List<ICompletionProposal> completions = new ArrayList<>();

        IDocument doc = viewer.getDocument();
        try {
            // check if we're in the middle of a word
            String prefix = completionWord(doc, offset);

            int replacementOffset = offset;
            int replacementLength = 0;
            if (prefix != null) {
                // replacing what's been typed so far
                replacementLength = prefix.length();
                // subtract prefix length from offset
                replacementOffset -= replacementLength;
            }
            String toolName = getToolName(doc, replacementOffset);
            String[] words = getCompletionStrings(prefix, toolName);
            for (String word : words) {
                completions.add(new CompletionProposal(word, replacementOffset, replacementLength, word.length()));
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        return completions.toArray(new ICompletionProposal[completions.size()]);
    }

    @Override
    public IContextInformation[] computeContextInformation(ITextViewer viewer,
            int offset) {
        return new IContextInformation[0];
    }

    @Override
    public char[] getCompletionProposalAutoActivationCharacters() {
        return null;
    }

    @Override
    public char[] getContextInformationAutoActivationCharacters() {
        return null;
    }

    @Override
    public IContextInformationValidator getContextInformationValidator() {
        return null;
    }

    @Override
    public String getErrorMessage() {
        return null;
    }

    private String completionWord(IDocument doc, int offset)
            throws BadLocationException {
        String word = null;
        if (offset > 0) {
            for (int n = offset - 1; n >= 0 && word == null; n--) {
                char c = doc.getChar(n);
                if (!Character.isLetterOrDigit(c)) {
                    word = doc.get(n + 1, offset - n - 1);
                }
                else if (n == 0) {
                    // beginning of file
                    word = doc.get(0, offset - n);
                }
            }
        }
        return word;
    }

    private String[] getCompletionStrings(String prefix, String toolName) {
        List<String> words = new ArrayList<>();

        // If the cursor is after "Memcheck:"
        if (toolName != null && toolName.equals(SuppressionsElementScanner.MEMCHECK)) {
            for (String word : SuppressionsElementScanner.MEMCHECK_SUPP_TYPES) {
                if (prefix == null || word.startsWith(prefix)) {
                    words.add(word);
                }
            }
        } else {
            if (prefix == null || SuppressionsElementScanner.MEMCHECK.startsWith(prefix)) {
                words.add(SuppressionsElementScanner.MEMCHECK + ":"); //$NON-NLS-1$
            }

            for (String word : SuppressionsElementScanner.CONTEXTS) {
                if (prefix == null || word.startsWith(prefix)) {
                    words.add(word + ":"); //$NON-NLS-1$
                }
            }
        }
        return words.toArray(new String[words.size()]);
    }

    private String getToolName(IDocument doc, int offset) throws BadLocationException {
        String tool = null;
        if (offset > 0) {
            char c = doc.getChar(--offset);
            // syntax is "toolName:suppressionKind"
            if (c == ':' && offset > 0) {
                for (int n = offset - 1; n >= 0 && tool == null; n--) {
                    c = doc.getChar(n);
                    if (!Character.isLetter(c)) {
                        tool = doc.get(n + 1, offset - n - 1);
                    } else if (n == 0) {
                        // Beginning of file
                        tool = doc.get(0, offset - n);
                    }
                }
            }
        }
        return tool;
    }

}
