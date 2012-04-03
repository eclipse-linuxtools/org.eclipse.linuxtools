/*******************************************************************************
 * Copyright (c) 2008 Phil Muldoon <pkmuldoon@picobot.org>.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Phil Muldoon <pkmuldoon@picobot.org> - initial API and implementation. 
 *******************************************************************************/
package org.eclipse.linuxtools.internal.systemtap.backup.ui.editor;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

class STPCompletionProcessor implements IContentAssistProcessor {

	private final IContextInformation[] NO_CONTEXTS = new IContextInformation[0];
	private final char[] PROPOSAL_ACTIVATION_CHARS = new char[] { '.' };
	private ICompletionProposal[] NO_COMPLETIONS = new ICompletionProposal[0];

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#computeCompletionProposals(org.eclipse.jface.text.ITextViewer, int)
	 */
	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer,
			int offset) {

		IDocument document = viewer.getDocument();

		String prefix;
		int locationOffset =0;

		// Get completion hint from document
		try {
			prefix = completionWord(document, offset);
			locationOffset = completionReplaceWordLocation(document, offset);
		} catch (Exception e) {
			return NO_COMPLETIONS;
		}

		// If cannot find a place to replace our partial typed completion
		// with the full one, abort with no completions.
		if (locationOffset < 0)
			return NO_COMPLETIONS;
			
		String[] completionData = STPMetadataSingleton
				.getCompletionResults(prefix);

		// If cannot find  any completions
		// abort with no completions.
		if (completionData.length < 1)
			return NO_COMPLETIONS;

		// Build proposals and submit
		ICompletionProposal[] result = new ICompletionProposal[completionData.length];
		for (int i = 0; i < completionData.length; i++)
			result[i] = new CompletionProposal(
							completionData[i].substring(offset - locationOffset), 
							offset, 0, completionData[i].length(), null, 
							completionData[i], null, null);
		return result;
	}

	/**
	 * 
	 * Compute location of completion proposal insertion.
	 * 
	 * @param doc - document to insert completion.
	 * @param offset - offset of where completion hint was first generated.
	 * @return - offset into document for completion proposal insertion.
	 * @throws BadLocationException 
	 * 
	 */
	private int completionReplaceWordLocation(IDocument doc, int offset) 
		throws BadLocationException {
		try {
			for (int n = offset-1; n >= 0; n--) {
				if (doc.getChar(n) == '.')
					return n+1;
			}
		} catch (BadLocationException e) {
			throw e;
		}

		return -1;
	}
	
	/**
	 * 
	 * Return the word the user wants to submit for completion proposals.
	 * 
	 * @param doc - document to insert completion.
	 * @param offset - offset of where completion hint was first generated.
	 * @return - word to generate completion proposals.
	 * 
	 * @throws BadLocationException
	 */
	private String completionWord(IDocument doc, int offset)
			throws BadLocationException {
		try {
			for (int n = offset - 1; n >= 0; n--) {
				char c = doc.getChar(n);
				if ((Character.isSpaceChar(c)) || (c == '\n') || (c == '\0')) {
					String word = doc.get(n + 1, offset - n - 1);
					if (word.charAt(word.length() - 1) == '.')
						return word.substring(0, word.length() - 1);
					else
						return word;
				}
			}
		} catch (BadLocationException e) {
			throw e;
		}
		return "";
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#computeContextInformation(org.eclipse.jface.text.ITextViewer, int)
	 */
	@Override
	public IContextInformation[] computeContextInformation(ITextViewer viewer,
			int offset) {
		return NO_CONTEXTS;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getCompletionProposalAutoActivationCharacters()
	 */
	@Override
	public char[] getCompletionProposalAutoActivationCharacters() {
		return PROPOSAL_ACTIVATION_CHARS;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getContextInformationAutoActivationCharacters()
	 */
	@Override
	public char[] getContextInformationAutoActivationCharacters() {
		return PROPOSAL_ACTIVATION_CHARS;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getContextInformationValidator()
	 */
	@Override
	public IContextInformationValidator getContextInformationValidator() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getErrorMessage()
	 */
	@Override
	public String getErrorMessage() {
		// TODO: When does this trigger?
		return "Error.";
	}
}