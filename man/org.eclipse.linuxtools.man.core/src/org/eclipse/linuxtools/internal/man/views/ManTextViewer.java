/*******************************************************************************
 * Copyright (c) 2009 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alexander Kurtakov - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.man.views;

import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.linuxtools.internal.man.parser.ManDocument;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.widgets.Composite;

/**
 * Text viewer for a man page.
 *
 */
public class ManTextViewer extends SourceViewer {

    /**
     * Creates a resizable text viewer.
     *
     * @param parent
     */
    public ManTextViewer(Composite parent) {
        super(parent, null, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        // setEditable(false);
    }

    /**
     * Sets the document to display.
     *
     * @param document
     *            The document to display.
     */
    public void setDocument(ManDocument document) {
        super.setDocument(document);
        TextPresentation style = new TextPresentation();
        for (int underlineSymbol : document.getUnderlinedSymbols()) {
            StyleRange styleRange = new StyleRange(underlineSymbol, 1, null,
                    null, SWT.NORMAL);
            styleRange.underline = true;
            style.addStyleRange(styleRange);
        }
        for (int boldSymbol : document.getBoldSymbols()) {
            style.mergeStyleRange(new StyleRange(boldSymbol, 1, null, null,
                    SWT.BOLD));
        }
        getTextWidget().setBackground(
                getControl().getDisplay().getSystemColor(SWT.COLOR_GRAY));
        changeTextPresentation(style, true);
    }

}
