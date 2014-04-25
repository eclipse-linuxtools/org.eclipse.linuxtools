/*******************************************************************************
 * Copyright (c) 2007, 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.ui.editor.hyperlink;

import java.util.ArrayList;
import java.util.List;

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
import org.eclipse.linuxtools.rpm.ui.editor.utils.RPMUtils;
import org.eclipse.ui.part.FileEditorInput;

/**
 * Detects values for Patch and Source definitions.
 *
 */
public class SourcesFileHyperlinkDetector extends AbstractHyperlinkDetector {

    private SpecfileEditor editor;
    private static final String PATCH_IDENTIFIER = "Patch"; //$NON-NLS-1$
    private static final String SOURCE_IDENTIFIER = "Source"; //$NON-NLS-1$
    private static final String URL_IDENTIFIER = "URL"; //$NON-NLS-1$

    /**
     * @see org.eclipse.jface.text.hyperlink.IHyperlinkDetector#detectHyperlinks(org.eclipse.jface.text.ITextViewer,
     *      org.eclipse.jface.text.IRegion, boolean)
     */
    @Override
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
        List<IHyperlink> tempHList = new ArrayList<>();
        // !! it feels like there is duplicate code, fix that !!
        if (editor.getEditorInput() instanceof FileEditorInput) {
            IFile original = ((FileEditorInput) editor.getEditorInput())
                    .getFile();
            if (line.startsWith(SOURCE_IDENTIFIER)
                    || line.startsWith(PATCH_IDENTIFIER)
                    || line.startsWith(URL_IDENTIFIER)) {
                int delimiterIndex = line.indexOf(':') + 1;
                String identifierValue = line.substring(delimiterIndex).trim();
                boolean validURL = RPMUtils.isValidUrl(identifierValue);
                // if valid URL, get its file name; else make file name the original identifier value
                String fileName = validURL ? RPMUtils.getURLFilename(identifierValue) : identifierValue;
                String resolvedFileName = UiUtils.resolveDefines(editor.getSpecfile(), fileName);
                boolean fileExists = RPMUtils.fileExistsInSources(original, resolvedFileName);
                if (region.getOffset() > lineInfo.getOffset()
                        + line.indexOf(identifierValue)) {
                    IRegion fileNameRegion = new Region(lineInfo.getOffset()
                            + line.indexOf(identifierValue), identifierValue.length());
                    if (fileExists) {
                        // add "Open" file option
                        tempHList.add(new SourcesFileHyperlink(original, resolvedFileName, fileNameRegion));
                    } else {
                        if (line.startsWith(PATCH_IDENTIFIER) && !identifierValue.endsWith("/")) { //$NON-NLS-1$
                            // add "Create" patch option using filename
                            tempHList.add(new SourcesFileCreateHyperlink(original, resolvedFileName, fileNameRegion));
                        }
                    }
                    // if valid URL and has a valid file
                    if (validURL && !identifierValue.endsWith("/")) { //$NON-NLS-1$
                        // add "Download" option
                        tempHList.add(new SourcesFileDownloadHyperlink(original, UiUtils.resolveDefines(editor.getSpecfile(), identifierValue), fileNameRegion));
                    }
                }
            }
        }

        return tempHList.isEmpty() ? null : tempHList.toArray(new IHyperlink[tempHList.size()]);
    }

    public void setEditor(SpecfileEditor editor) {
        this.editor = editor;
    }
}
