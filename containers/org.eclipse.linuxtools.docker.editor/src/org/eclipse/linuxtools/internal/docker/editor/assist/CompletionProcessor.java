/*******************************************************************************
 * Copyright (c) 2015-2016 Open Analytics NV and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.editor.assist;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.linuxtools.internal.docker.editor.Activator;
import org.eclipse.linuxtools.internal.docker.editor.scanner.InstructionWordRule;
import org.eclipse.linuxtools.internal.docker.editor.util.AssetLoader;

public class CompletionProcessor implements IContentAssistProcessor {

	private static final ICompletionProposal[] NO_COMPLETIONS = {};
	private static final IContextInformation[] NO_CONTEXTS = {};
	private static final char[] PROPOSAL_ACTIVATION_CHARS = {};
	private static final char[] INFO_ACTIVATION_CHARS = {};
	
	private static final String ADDITIONAL_INFO_PATH = "assets/additional-info";
	private Map<String, String> additionalInfos = new HashMap<String, String>();
	
	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
		try {
			IDocument document = viewer.getDocument();
			ArrayList<ICompletionProposal> result = new ArrayList<ICompletionProposal>();
			
			int lineNr = document.getLineOfOffset(offset);
			int lineOffset = document.getLineOffset(lineNr);
			
			boolean isNewLine = (offset == 0) || (document.getChar(offset - 1) == '\n');
			if (isNewLine) {
				for (String instr: InstructionWordRule.INSTRUCTIONS) result.add(new CompletionProposal(instr, lineOffset, 0, getAdditionalInfo(instr)));
			}

			boolean isFirstWord = true;
			for (int i=lineOffset; i<offset; i++) {
				if (Character.isWhitespace(document.getChar(i))) isFirstWord = false;
			}
			if (isFirstWord) {
				String prefix = "";
				for (int i=lineOffset; i<offset; i++) prefix += document.getChar(i);
				for (String instr: InstructionWordRule.INSTRUCTIONS) {
					if (instr.toLowerCase().startsWith(prefix.toLowerCase())) result.add(new CompletionProposal(instr, lineOffset, prefix.length(), getAdditionalInfo(instr)));
				}	
			}

			return result.toArray(new ICompletionProposal[result.size()]);
		} catch (Exception e) {
			Activator.getDefault().getLog().log(new Status(IStatus.WARNING, Activator.PLUGIN_ID, "Failed to compute completion proposals", e));
			return NO_COMPLETIONS;
		}
	}

	@Override
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
		return NO_CONTEXTS;
	}

	@Override
	public char[] getCompletionProposalAutoActivationCharacters() {
		return PROPOSAL_ACTIVATION_CHARS;
	}

	@Override
	public char[] getContextInformationAutoActivationCharacters() {
		return INFO_ACTIVATION_CHARS;
	}

	@Override
	public String getErrorMessage() {
		return "";
	}

	@Override
	public IContextInformationValidator getContextInformationValidator() {
		return null;
	}

	private String getAdditionalInfo(String instruction) {
		if (additionalInfos.containsKey(instruction)) return additionalInfos.get(instruction);
		
		String additionalInfo = "";
		String targetFile = ADDITIONAL_INFO_PATH + "/" + instruction + ".html";
		try {
			byte[] htmlContents = AssetLoader.loadAsset(targetFile);
			if (htmlContents != null) additionalInfo = new String(htmlContents);
		} catch (IOException e) {
			Activator.log(IStatus.WARNING, "Failed to load additional info file for instruction " + instruction, e);
		}
		
		additionalInfos.put(instruction, additionalInfo);
		return additionalInfo;
	}
}