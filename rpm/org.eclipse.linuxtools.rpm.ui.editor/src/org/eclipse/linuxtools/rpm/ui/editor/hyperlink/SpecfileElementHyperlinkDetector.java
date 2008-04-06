/*******************************************************************************
 * Copyright (c) 2008 Alexander Kurtakov.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Kurtakov - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.rpm.ui.editor.hyperlink;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.linuxtools.rpm.ui.editor.parser.Specfile;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileDefine;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileElement;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileSource;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

import com.ibm.icu.util.StringTokenizer;

/**
 * Hyperlink detector for source, patch and defines in the spec file.
 * 
 */
public class SpecfileElementHyperlinkDetector extends AbstractHyperlinkDetector {

	private static final String PATCH_IDENTIFIER = "%patch";
	private static final String SOURCE_IDENTIFIER = "%{SOURCE";
	private Specfile specfile;

	public SpecfileElementHyperlinkDetector(Specfile specfile) {
		this.specfile = specfile;
	}

	public IHyperlink[] detectHyperlinks(ITextViewer textViewer,
			IRegion region, boolean canShowMultipleHyperlinks) {

		if (region == null || textViewer == null) {
			return null;
		}

		IDocument document = textViewer.getDocument();

		int offset = region.getOffset();

		if (document == null) {
			return null;
		}
		IRegion lineInfo;
		String line;
		try {
			lineInfo = document.getLineInformationOfOffset(offset);
			line = document.get(lineInfo.getOffset(), lineInfo.getLength());
		} catch (BadLocationException ex) {
			return null;
		}

		int offsetInLine = offset - lineInfo.getOffset();

		StringTokenizer tokens = new StringTokenizer(line);
		String word = "";
		int tempLineOffset = 0;
		while (tokens.hasMoreTokens()) {
			String tempWord = tokens.nextToken();
			tempLineOffset += tempWord.length();
			word = tempWord;
			if (tempLineOffset > offsetInLine) {
				break;
			}
		}
		if (word.startsWith(SOURCE_IDENTIFIER)) {
			int sourceNumber = Integer.valueOf(
					word.substring(SOURCE_IDENTIFIER.length(),
							word.length() - 1)).intValue();
			SpecfileSource source = specfile.getSource(sourceNumber);
			return prepareHyperlink(lineInfo, line, word, document, source);
		} else if (word.startsWith(PATCH_IDENTIFIER)) {

			int sourceNumber = Integer.valueOf(
					word.substring(PATCH_IDENTIFIER.length(), word.length()))
					.intValue();
			SpecfileSource source = specfile.getPatch(sourceNumber);
			return prepareHyperlink(lineInfo, line, word, document, source);
		} else {
			String defineName = getDefineName(word);
			SpecfileDefine define = specfile.getDefine(defineName);
			if (define != null) {
				return prepareHyperlink(lineInfo, line, defineName, document,
						define);
			}
		}
		return null;
	}

	private String getDefineName(String word) {
		if (word.startsWith("%{")) {
			return word.substring(2, word.length() - 1);
		}
		return null;
	}

	private IHyperlink[] prepareHyperlink(IRegion lineInfo, String line,
			String word, IDocument document, SpecfileElement source) {
		IRegion urlRegion = new Region(lineInfo.getOffset()
				+ line.indexOf(word), word.length());

		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
		IWorkbenchPage page = win.getActivePage();
		IEditorPart editor = page.getActiveEditor();
		IFile original = ((FileEditorInput) editor.getEditorInput()).getFile();
		return new IHyperlink[] { new SpecfileElementHyperlink(urlRegion,
				source, original) };
	}
}
