/*******************************************************************************
 * Copyright (c) 2007 Alphonse Van Assche and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alphonse Van Assche - initial API and implementation
 *    Red Hat Inc. - ongoing maintenance
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.rpmlint.resolutions;import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.linuxtools.internal.rpm.rpmlint.RpmlintLog;
import org.eclipse.linuxtools.rpm.ui.editor.SpecfileEditor;
;

/**
 * Defines the common functionallity for resolution which fix is to replace text in a line.
 */
public abstract class AReplaceTextResolution extends ARpmlintResolution {

    /**
     * Returns the original string.
     *
     * @return The original string.
     */
    public abstract String getOriginalString();

    /**
     * Returns the string to replace in the <code>Document</code>.
     *
     * @return The string to replace.
     *
     */
    public abstract String getReplaceString();

    @Override
    public void run(IMarker marker) {

        SpecfileEditor editor = getEditor(marker);
        if (editor == null) {
            return;
        }
        IDocument doc = (IDocument) editor.getAdapter(IDocument.class);

        try {
            int lineNumber = marker.getAttribute(IMarker.LINE_NUMBER, 0);
            int index = doc.getLineOffset(lineNumber);
            String line = editor.getSpecfile().getLine(lineNumber);
            int rowIndex = line.indexOf(getOriginalString());
            if (rowIndex > -1){
                doc.replace(index+rowIndex, getOriginalString().length(), getReplaceString());
            }
        } catch (BadLocationException e) {
            RpmlintLog.logError(e);
        }
    }


}
