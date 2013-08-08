/*******************************************************************************
 * Copyright (c) 2007, 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *    Neil Guzman - create patches hyperlink (B#413508)
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.ui.editor.hyperlink;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
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
		boolean patchExists = false;
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
				patchExists = patchExists(original, fileName);
				if (region.getOffset() > lineInfo.getOffset()
						+ line.indexOf(fileName)) {
					IRegion fileNameRegion = new Region(lineInfo.getOffset()
							+ line.indexOf(fileName), fileName.length());
					if (line.startsWith(PATCH_IDENTIFIER) && !patchExists) {
						return new IHyperlink[] {
								new SourcesFileCreateHyperlink(original, UiUtils
										.resolveDefines(editor.getSpecfile(),
												fileName), fileNameRegion) };
					} else {
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
		}

		return null;
	}
	
	public void setEditor(SpecfileEditor editor) {
		this.editor = editor;
	}

	/**
	 * Helper method to check if the patch file exists within the
	 * current project.
	 *
	 * @return True if the file exists
	 */
	private boolean patchExists(IFile original, String patchName) {
		boolean rc = true;
		IContainer container = original.getParent();
		IResource resourceToOpen = container.findMember(patchName);
		IFile file = null;

		if (resourceToOpen == null) {
			IResource sourcesFolder = container.getProject().findMember(
					"SOURCES"); //$NON-NLS-1$
			file = container.getFile(new Path(patchName));
			if (sourcesFolder != null) {
				file = ((IFolder) sourcesFolder).getFile(new Path(patchName));
			}
			if (!file.exists()) {
				rc = false;
			}
		}

		return rc;
	}
}
