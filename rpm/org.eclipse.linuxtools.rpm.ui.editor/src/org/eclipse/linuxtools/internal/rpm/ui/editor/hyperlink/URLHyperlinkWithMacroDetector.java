/*******************************************************************************
 * Copyright (c) 2007 Alphonse Van Assche and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alphonse Van Assche
 *     Andrew Overholt
 *******************************************************************************/

package org.eclipse.linuxtools.internal.rpm.ui.editor.hyperlink;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.URLHyperlink;
import org.eclipse.jface.text.hyperlink.URLHyperlinkDetector;
import org.eclipse.linuxtools.internal.rpm.ui.editor.UiUtils;
import org.eclipse.linuxtools.rpm.ui.editor.SpecfileEditor;
import org.eclipse.linuxtools.rpm.ui.editor.parser.Specfile;


/**
 * URL hyperlink with macro detector.
 * derived form the JFace URLHyperlinkDetector class
 *
 */
public class URLHyperlinkWithMacroDetector extends URLHyperlinkDetector {

    private Specfile specfile;

    @Override
    public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks) {
        if (specfile == null) {
            SpecfileEditor a = ((SpecfileEditor) this.getAdapter(SpecfileEditor.class));
            if (a != null) {
                specfile = a.getSpecfile();
            } else {
                return null;
            }
        }
        IHyperlink[] returned = super.detectHyperlinks(textViewer, region, canShowMultipleHyperlinks);
        if (returned != null && returned.length > 0) {
        IHyperlink hyperlink = returned[0];
            if (hyperlink instanceof URLHyperlink) {
                URLHyperlink urlHyperlink = (URLHyperlink) hyperlink;
                String newURLString = UiUtils.resolveDefines(specfile, urlHyperlink.getURLString());
                return new IHyperlink[] {new URLHyperlink(urlHyperlink.getHyperlinkRegion(), newURLString)};
            }
        }
        return returned;
    }

    public void setSpecfile(Specfile specfile) {
        this.specfile = specfile;
    }

}
