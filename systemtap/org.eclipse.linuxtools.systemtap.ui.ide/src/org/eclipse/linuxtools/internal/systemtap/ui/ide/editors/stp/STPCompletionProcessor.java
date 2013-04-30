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
import java.util.Arrays;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.TapsetLibrary;

public class STPCompletionProcessor implements IContentAssistProcessor, ITextHover {

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

	private static class Token implements IRegion{
		String tokenString;
		int offset;

		public Token(String string, int n) {
			this.tokenString = string;
			this.offset = n;
		}

		@Override
		public int getLength() {
			return this.tokenString.length();
		}

		@Override
		public int getOffset() {
			return this.offset;
		}
	}

	public STPCompletionProcessor(){
		this.stpMetadataSingleton = STPMetadataSingleton.getInstance();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#computeCompletionProposals(org.eclipse.jface.text.ITextViewer, int)
	 */
	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer,
			int offset) {
		return computeCompletionProposals(viewer.getDocument(), offset);
	}

	public ICompletionProposal[] computeCompletionProposals(IDocument document, int offset){

		ITypedRegion partition = null;

		try {
			partition = document.getPartition(offset);
		} catch (BadLocationException e1) {
			return NO_COMPLETIONS;
		}

		String prefix = ""; //$NON-NLS-1$
		String prePrefix = ""; //$NON-NLS-1$

		// Get completion hint from document
		try {
			prefix = getPrefix(document, offset);
			Token previousToken = getPrecedingToken(document, offset - prefix.length() - 1);

			while (previousToken.tokenString.equals("=") || //$NON-NLS-1$
					previousToken.tokenString.equals(",") ){ //$NON-NLS-1$
				previousToken = getPrecedingToken(document, previousToken.offset - 1);
				previousToken = getPrecedingToken(document, previousToken.offset - 1);
			}

			prePrefix = previousToken.tokenString;

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

		// If inside a probe return probe variable completions and functions
		// which can be called.
		if (partition.getType() == STPPartitionScanner.STP_PROBE){
			ICompletionProposal[] variableCompletions = getProbeVariableCompletions(document, offset, prefix);
			ICompletionProposal[] functionCompletions = getFunctionCompletions(offset, prefix);

			ArrayList<ICompletionProposal> completions = new ArrayList<ICompletionProposal>(
					variableCompletions.length + functionCompletions.length);
			completions.addAll(Arrays.asList(variableCompletions));
			completions.addAll(Arrays.asList(functionCompletions));

			return completions.toArray(new ICompletionProposal[0]);
		}

		return NO_COMPLETIONS;
	}

	private ICompletionProposal[] getFunctionCompletions(int offset,
			String prefix) {
		String[] completionData = stpMetadataSingleton.getFunctionCompletions(prefix);
		ICompletionProposal[] result = new ICompletionProposal[completionData.length];
		int prefixLength = prefix.length();
		for (int i = 0; i < completionData.length; i++){
			result[i] = new CompletionProposal(
							completionData[i].substring(prefixLength) + "()", //$NON-NLS-1$
							offset,
							0,
							completionData[i].length() - prefixLength + 1,
							null,
							completionData[i] + " - function", //$NON-NLS-1$
							null,
							TapsetLibrary.getAndCacheDocumentation("function::" + completionData[i])); //$NON-NLS-1$
		}

		return result;
	}

	private ICompletionProposal[] getProbeVariableCompletions(IDocument document, int offset, String prefix){
		try {
			String probe;
			probe = getProbe(document, offset);
			String[] completionData = stpMetadataSingleton
					.getProbeVariableCompletions(probe, prefix);
			ICompletionProposal[] result = new ICompletionProposal[completionData.length];

			int prefixLength = prefix.length();
			for (int i = 0; i < completionData.length; i++) {
				int endIndex = completionData[i].indexOf(':');
				String variableName = completionData[i].substring(0, endIndex);
				result[i] = new CompletionProposal(completionData[i].substring(
						prefixLength, endIndex),
						offset,
						0,
						endIndex - prefixLength,
						null,
						completionData[i] + " - variable", //$NON-NLS-1$
						null,
						TapsetLibrary
								.getAndCacheDocumentation("probe::" + probe + "::" + variableName)); //$NON-NLS-1$ //$NON-NLS-2$
			}
			return result;
		} catch (BadLocationException e) {
			return NO_COMPLETIONS;
		}
	}

	/**
	 * Returns the full name of the probe surrounding the given
	 * offset. This function assumes that the given offset is inside
	 * of a {@link STPPartitionScanner#STP_PROBE} section.
	 * @param document
	 * @param offset
	 * @return the probe name
	 * @throws BadLocationException
	 */
	private String getProbe(IDocument document, int offset) throws BadLocationException{
		String probePoint = null;

		ITypedRegion partition = document.getPartition(offset);
		String probe = document.get(partition.getOffset(), partition.getLength());

		// make sure that we are inside a probe
		if (probe.startsWith(PROBE_KEYWORD)){
			probePoint = probe.substring(PROBE_KEYWORD.length(), probe.indexOf('{'));
			probePoint = probePoint.trim();
		}

		return probePoint;
	}

	private ICompletionProposal[] getProbeCompletionList(String prefix, int offset){
		prefix = canonicalizePrefix(prefix);
		String[] completionData = stpMetadataSingleton.getCompletionResults(prefix);

		String manPrefix = "probe::"; //$NON-NLS-1$
		if (prefix.indexOf('.') == -1){
			manPrefix = "tapset::"; //$NON-NLS-1$
		}

		// Build proposals and submit
		ICompletionProposal[] result = new ICompletionProposal[completionData.length];
		for (int i = 0; i < completionData.length; i++) {
			result[i] = new CompletionProposal(
							completionData[i].substring(prefix.length()),
							offset,
							0,
							completionData[i].length() - prefix.length(),
							null,
							completionData[i],
							null,
							TapsetLibrary.getAndCacheDocumentation(manPrefix + completionData[i]));
		}
		return result;

	}

	/**
	 * Returns a standardized version of the given prefix so that completion matching
	 * can be performed.
	 * For example for process("/some/long/path") this returns process("PATH");
	 * @param prefix
	 * @return
	 */
	private String canonicalizePrefix(String prefix) {

		if (prefix.isEmpty())
		 {
			return ""; //$NON-NLS-1$
		}

		if(prefix.matches("process\\(\".*\"\\).*")){ //$NON-NLS-1$
			prefix = prefix.replaceAll("process\\(\".*\"\\)", "process\\(\"PATH\"\\)"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		if(prefix.matches("process\\(\\d*\\).*")){ //$NON-NLS-1$
			prefix = prefix.replaceAll("process\\(\\d*\\)", "process\\(PID\\)"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		if(prefix.matches("procfs\\(\".*\"\\).*")){ //$NON-NLS-1$
			prefix = prefix.replaceAll("procfs\\(\".*\"\\)", "procfs\\(\"PATH\"\\)"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		if(prefix.matches(".*function\\(\".*\"\\).*")){ //$NON-NLS-1$
			prefix = prefix.replaceAll("function\\(\".*\"\\)", "function\\(\"PATTERN\"\\)"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		if(prefix.matches(".*module\\(\".*\"\\).*")){ //$NON-NLS-1$
			prefix = prefix.replaceAll("module\\(\".*\"\\)", "module\\(\"MPATTERN\"\\)"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		if(prefix.matches("jiffies\\(\\d*\\).*")){ //$NON-NLS-1$
			prefix = prefix.replaceAll("jiffies\\(\\d*\\)", "jiffies\\(N\\)"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		if(prefix.matches("randomize\\(\\d*\\).*")){ //$NON-NLS-1$
			prefix = prefix.replaceAll("randomize\\(\\d*\\)", "randomize\\(M\\)"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		return prefix;
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
	private Token getPrecedingToken(IDocument doc, int offset) throws BadLocationException{
		// Skip trailing space
		int n = offset;
		while (n >= 0 && Character.isSpaceChar(doc.getChar(n))){
			n--;
		}

		char c = doc.getChar(n);
		if(isTokenDelimiter(c)){
			return new Token(Character.toString(c), n);
		}

		int end = n;
		while (n >= 0 && !isTokenDelimiter((doc.getChar(n)))){
			n--;
		}

		return new Token(doc.get(n+1, end-n), n+1);
	}

	private Token getCurrentToken(IDocument doc, int offset) throws BadLocationException{
		char c = doc.getChar(offset);

		if(isDelimiter(c)){
			return new Token(Character.toString(c), offset);
		}

		int start = offset;
		while (start >= 0 && !isDelimiter((doc.getChar(start)))){
			start--;
		}

		int end = offset;
		while (end < doc.getLength() && !isDelimiter((doc.getChar(end)))){
			end++;
		}

		start++;
		return new Token(doc.get(start, end-start), start);
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
			if (isTokenDelimiter(c)) {
				String word = doc.get(n + 1, offset - n - 1);
				return word;
			}
		}
		return ""; //$NON-NLS-1$
	}

	private boolean isTokenDelimiter(char c) {
		if (Character.isWhitespace(c)) {
			return true;
		}

		switch (c) {
		case '\n':
		case '\0':
		case ',':
		case '{':
		case '}':
			return true;
		}
		return false;
	}

	private boolean isDelimiter (char c) {

		if (isTokenDelimiter(c)) {
			return true;
		}

		switch (c) {
		case '(':
		case ')':
			return true;
		}
		return false;
	}

	public void waitForInitialization(){
		this.stpMetadataSingleton.waitForInitialization();
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
		return "Error."; //$NON-NLS-1$
	}

	@Override
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		String documentation = null;
		try {
			String keyword = textViewer.getDocument().get(hoverRegion.getOffset(), hoverRegion.getLength());

			documentation = TapsetLibrary.getDocumentation("function::" + keyword); //$NON-NLS-1$
			if (!documentation.startsWith("No manual entry for")){ //$NON-NLS-1$
				return documentation;
			}

			documentation = TapsetLibrary.getDocumentation("probe::" + keyword); //$NON-NLS-1$
			if (!documentation.startsWith("No manual entry for")){ //$NON-NLS-1$
				return documentation;
			}

			documentation = TapsetLibrary.getDocumentation("tapset::" + keyword); //$NON-NLS-1$
			if (!documentation.startsWith("No manual entry for")){ //$NON-NLS-1$
				return documentation;
			}

			if (keyword.indexOf('.') > 0){
				keyword = keyword.split("\\.")[0]; //$NON-NLS-1$
				documentation = TapsetLibrary.getDocumentation("tapset::" + keyword); //$NON-NLS-1$
			}

			if (textViewer.getDocument().getPartition(hoverRegion.getOffset())
					.getType() == STPPartitionScanner.STP_PROBE) {
				String probe = getProbe(textViewer.getDocument(), hoverRegion.getOffset());
				documentation = TapsetLibrary.getDocumentation("probe::" + probe + "::"+ keyword); //$NON-NLS-1$ //$NON-NLS-2$
			}

		} catch (BadLocationException e) {
			// Bad hover location; just ignore it.
		}

		return documentation;
	}

	@Override
	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
		try {
			return getCurrentToken(textViewer.getDocument(), offset);
		} catch (BadLocationException e) {
			// Bad hover location; just ignore it.
		}

		return null;
	}
}