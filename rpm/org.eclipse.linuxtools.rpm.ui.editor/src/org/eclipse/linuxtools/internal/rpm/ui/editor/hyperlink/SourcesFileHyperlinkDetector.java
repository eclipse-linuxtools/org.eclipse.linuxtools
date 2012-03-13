/*******************************************************************************
 * Copyright (c) 2007, 2010 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.ui.editor.hyperlink;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.linuxtools.internal.rpm.ui.editor.UiUtils;
import org.eclipse.linuxtools.rpm.ui.editor.SpecfileEditor;
import org.eclipse.ui.part.FileEditorInput;

/**
 * Detects values for Patch and Source definitions.
 * 
 */
public class SourcesFileHyperlinkDetector extends AbstractHyperlinkDetector {

	SpecfileEditor editor;
	private static final String PATCH_IDENTIFIER = "Patch"; //$NON-NLS-1$
	private static final String SOURCE_IDENTIFIER = "Source"; //$NON-NLS-1$

	/**
	 * @see org.eclipse.jface.text.hyperlink.IHyperlinkDetector#detectHyperlinks(org.eclipse.jface.text.ITextViewer,
	 *      org.eclipse.jface.text.IRegion, boolean)
	 */
	public IHyperlink[] detectHyperlinks(ITextViewer textViewer,
			IRegion region, boolean canShowMultipleHyperlinks) {
		if (region == null || textViewer == null) {
			return null;
		}
		
		if (editor == null) {
			editor = ((SpecfileEditor) this.getAdapter(SpecfileEditor.class));
			if (editor == null) {
				return null;
			}
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
		if (editor.getEditorInput() instanceof FileEditorInput) {
			IFile original = ((FileEditorInput) editor.getEditorInput())
					.getFile();
			if (line.startsWith(SOURCE_IDENTIFIER)
					|| line.startsWith(PATCH_IDENTIFIER)) {
				int delimiterIndex = line.indexOf(':') + 1;
				String fileName = line.substring(delimiterIndex).trim();
				if (region.getOffset() > lineInfo.getOffset()
						+ line.indexOf(fileName)) {
					IRegion fileNameRegion = new Region(lineInfo.getOffset()
							+ line.indexOf(fileName), fileName.length());
					return new IHyperlink[] {
							new SourcesFileHyperlink(original, UiUtils
									.resolveDefines(editor.getSpecfile(),
											fileName), fileNameRegion),
							new SourcesFileDownloadHyperlink(original, UiUtils
									.resolveDefines(editor.getSpecfile(),
											fileName), fileNameRegion) };
				}
			}
		}

		return null;
	}
	
	public void setEditor(SpecfileEditor editor) {
		this.editor = editor;
	}
}
