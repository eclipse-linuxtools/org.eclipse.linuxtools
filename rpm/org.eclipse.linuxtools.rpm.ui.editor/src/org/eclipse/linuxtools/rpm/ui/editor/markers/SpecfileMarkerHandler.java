/*******************************************************************************
 * Copyright (c) 2008, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.ui.editor.markers;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.linuxtools.internal.rpm.ui.editor.SpecfileLog;

public abstract class SpecfileMarkerHandler {

    IDocument document;
    IFile file;

    public SpecfileMarkerHandler(IFile file, IDocument document) {
        this.file = file;
        this.document = document;
    }

    abstract String getMarkerID();

    protected Integer getCharOffset(int lineNumber, int columnNumber) {
        try {
            return document.getLineOffset(lineNumber)
                    + columnNumber;
        } catch (BadLocationException e) {
            SpecfileLog.logError(e);
            return null;
        }
    }

    public void removeExistingMarkers() {
        if (file == null || !file.exists()) {
            return;
        }

        try {
            file.deleteMarkers(getMarkerID(), true, IResource.DEPTH_ZERO);
        } catch (CoreException e1) {
            SpecfileLog.logError(e1);
        }
    }

    public void setFile(IFile file) {
        this.file = file;
    }

    public void setDocument(IDocument document) {
        this.document = document;
    }
}
