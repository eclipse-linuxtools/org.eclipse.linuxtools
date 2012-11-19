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
package org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp;

import java.util.ArrayList;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

public class STPCompletionProcessor implements IContentAssistProcessor {

	private final IContextInformation[] NO_CONTEXTS = new IContextInformation[0];
	private final char[] PROPOSAL_ACTIVATION_CHARS = new char[] { '.' };
	private ICompletionProposal[] NO_COMPLETIONS = new ICompletionProposal[0];

	private static final String GLOBAL_KEYWORD = "global "; //$NON-NLS-1$
	private static final String PROBE_KEYWORD = "probe "; //$NON-NLS-1$
	private static final String FUNCTION_KEYWORD = "function "; //$NON-NLS-1$

	private static final String[][] GLOBAL_KEYWORDS = {
			{ GLOBAL_KEYWORD, Messages.STPCompletionProcessor_global }, 
			{ PROBE_KEYWORD, Messages.STPCompletionProcessor_probe },
			{ FUNCTION_KEYWORD, Messages.STPCompletionProcessor_function } };

	private STPMetadataSingleton stpMetadataSingleton;

	public STPCompletionProcessor(){
		this.stpMetadataSingleton = STPMetadataSingleton.getInstance(); 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#computeCompletionProposals(org.eclipse.jface.text.ITextViewer, int)
	 */
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer,
			int offset) {
		return computeCompletionProposals(viewer.getDocument(), offset);
	}

	public ICompletionProposal[] computeCompletionProposals(IDocument document, int offset){

		ITypedRegion partition = null;
		
		try {
			partition = document.getPartition(offset);
		} catch (BadLocationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		String prefix = ""; //$NON-NLS-1$
		String prePrefix = ""; //$NON-NLS-1$

		// Get completion hint from document
		try {
			prefix = getPrefix(document, offset);
			prePrefix = getPrecedingToken(document, prefix, offset);
		} catch (BadLocationException e) {
			return NO_COMPLETIONS;
		}

		if (prePrefix.startsWith("probe")){ //$NON-NLS-1$
			return getProbeCompletionList(prefix, offset);
		}

		// In the global scope return global keyword completion.
		if (partition.getType() == IDocument.DEFAULT_CONTENT_TYPE ){
			return getGlobalKeywordCompletion(prefix, offset);
		}
		
		return NO_COMPLETIONS;
	}

	private ICompletionProposal[] getProbeCompletionList(String prefix, int offset){
		String[] completionData = stpMetadataSingleton.getCompletionResults(prefix);
		return buildCompletionList(offset, prefix.length(), completionData);
	}

	private ICompletionProposal[] buildCompletionList(int offset, int prefixLength,String[] completionData){
		// Build proposals and submit
		ICompletionProposal[] result = new ICompletionProposal[completionData.length];
		for (int i = 0; i < completionData.length; i++)
			result[i] = new CompletionProposal(
							completionData[i].substring(prefixLength), 
							offset,
							0,
							completionData[i].length() - prefixLength,
							null, 
							completionData[i],
							null,
							null);
		return result;
		
	}

	private ICompletionProposal[] getGlobalKeywordCompletion(String prefix, int offset) {

		ArrayList<ICompletionProposal> completions = new ArrayList<ICompletionProposal>();
		int prefixLength = prefix.length();
		for (String[] keyword : GLOBAL_KEYWORDS) {
			if (keyword[0].startsWith(prefix)){
				CompletionProposal proposal = new CompletionProposal(
						keyword[0].substring(prefixLength),
						offset,
						0,
						keyword[0].length() - prefixLength,
						null,
						keyword[0],
						new ContextInformation("contextDisplayString", "informationDisplayString"), //$NON-NLS-1$ //$NON-NLS-2$
						keyword[1]);
				completions.add(proposal);
			}
		}
		return completions.toArray(new ICompletionProposal[0]);
	}

	/**
	 * Returns the token preceding the current completion position.
	 * @param doc The document for the which the completion is requested.
	 * @param prefix The prefix for which the user has requested completion.
	 * @param offset Current offset in the document
	 * @return The preceding token.
	 * @throws BadLocationException
	 */
	private String getPrecedingToken(IDocument doc, String prefix, int offset) throws BadLocationException{
		// Skip trailing space
		int n = offset - prefix.length() - 1;
		while (n >= 0 && Character.isSpaceChar(doc.getChar(n))){
			n--;
		}
		return getPrefix(doc, n + 1);
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
	private String getPrefix(IDocument doc, int offset)
			throws BadLocationException {

		for (int n = offset - 1; n >= 0; n--) {
			char c = doc.getChar(n);
			if ((Character.isSpaceChar(c)) || (c == '\n') || (c == '\0')) {
				String word = doc.get(n + 1, offset - n - 1);
				return word;
			}
		}
		return ""; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#computeContextInformation(org.eclipse.jface.text.ITextViewer, int)
	 */
	public IContextInformation[] computeContextInformation(ITextViewer viewer,
			int offset) {
		return NO_CONTEXTS;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getCompletionProposalAutoActivationCharacters()
	 */
	public char[] getCompletionProposalAutoActivationCharacters() {
		return PROPOSAL_ACTIVATION_CHARS;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getContextInformationAutoActivationCharacters()
	 */
	public char[] getContextInformationAutoActivationCharacters() {
		return PROPOSAL_ACTIVATION_CHARS;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getContextInformationValidator()
	 */
	public IContextInformationValidator getContextInformationValidator() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getErrorMessage()
	 */
	public String getErrorMessage() {
		// TODO: When does this trigger?
		return "Error."; //$NON-NLS-1$
	}
}