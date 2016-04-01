/*******************************************************************************
 * Copyright (c) 2015-2016 Open Analytics NV and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.editor.assist;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension3;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

/**
 * Based on {@link org.eclipse.jface.text.contentassist.CompletionProposal} with
 * added functionality for showing additional information formatted as HTML.
 */
public class CompletionProposal implements ICompletionProposal, ICompletionProposalExtension3 {

	private String fReplacementString;
	private int fReplacementOffset;
	private int fReplacementLength;

	private String fAdditionalProposalInfo;

	public CompletionProposal(String replacementString, int replacementOffset, int replacementLength,
			String additionalProposalInfo) {
		this.fReplacementString = replacementString;
		this.fReplacementOffset = replacementOffset;
		this.fReplacementLength = replacementLength;
		this.fAdditionalProposalInfo = additionalProposalInfo;
	}

	@Override
	public void apply(IDocument document) {
		try {
			document.replace(fReplacementOffset, fReplacementLength, fReplacementString);
		} catch (BadLocationException x) {
			// ignore
		}
	}

	@Override
	public Point getSelection(IDocument document) {
		return new Point(fReplacementOffset + fReplacementString.length(), 0);
	}

	@Override
	public String getDisplayString() {
		return fReplacementString;
	}

	@Override
	public String getAdditionalProposalInfo() {
		return fAdditionalProposalInfo;
	}

	@Override
	public IInformationControlCreator getInformationControlCreator() {
		return new DefaultInformationControlCreator();
	}

	@Override
	public IContextInformation getContextInformation() {
		return null;
	}

	@Override
	public Image getImage() {
		return null;
	}

	@Override
	public CharSequence getPrefixCompletionText(IDocument document, int completionOffset) {
		return null;
	}

	@Override
	public int getPrefixCompletionStart(IDocument document, int completionOffset) {
		return 0;
	}

	private static class DefaultInformationControlCreator implements IInformationControlCreator {
		@Override
		public IInformationControl createInformationControl(Shell shell) {
			return new DefaultInformationControl(shell, true);
		}
	}
}