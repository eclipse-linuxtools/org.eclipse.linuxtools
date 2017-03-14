/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.ui.editor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension3;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension4;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension5;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

public class SpecCompletionProposal implements ICompletionProposal, ICompletionProposalExtension3,
		ICompletionProposalExtension4, ICompletionProposalExtension5 {

	private String fReplacementString;
	private int fReplacementOffset;
	private int fReplacementLength;
	private int fCursorPosition;
	private Image fImage;
	private String fAdditionalProposalInfo;
	private String fDisplayString;
	private IContextInformation fContextInformation;

	public SpecCompletionProposal(String replacementString, int replacementOffset, int replacementLength,
			int cursorPosition, Image image, String displayString, IContextInformation contextInformation,
			String additionalProposalInfo) {
		this.fReplacementString = replacementString;
		this.fReplacementOffset = replacementOffset;
		this.fReplacementLength = replacementLength;
		this.fAdditionalProposalInfo = additionalProposalInfo;
		this.fImage = image;
		this.fCursorPosition = cursorPosition;
		this.fDisplayString = displayString;
		this.fContextInformation = contextInformation;
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
		return new Point(fReplacementOffset + fCursorPosition, 0);
	}

	@Override
	public String getDisplayString() {
		if (fDisplayString != null)
			return fDisplayString;
		return fReplacementString;
	}

	@Override
	public String getAdditionalProposalInfo() {
		System.out.println(fAdditionalProposalInfo);
		return fAdditionalProposalInfo;
	}

	@Override
	public IInformationControlCreator getInformationControlCreator() {
		return shell -> new DefaultInformationControl(shell, true);
	}

	@Override
	public IContextInformation getContextInformation() {
		return fContextInformation;
	}

	@Override
	public Image getImage() {
		return fImage;
	}

	@Override
	public CharSequence getPrefixCompletionText(IDocument document, int completionOffset) {
		return null;
	}

	@Override
	public int getPrefixCompletionStart(IDocument document, int completionOffset) {
		return 0;
	}

	@Override
	public boolean isAutoInsertable() {
		return true;
	}

	@Override
	public Object getAdditionalProposalInfo(IProgressMonitor monitor) {
		return fAdditionalProposalInfo;
	}

}
