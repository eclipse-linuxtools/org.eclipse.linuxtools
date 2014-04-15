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
package org.eclipse.linuxtools.internal.rpm.rpmlint.resolutions;

import org.eclipse.core.resources.IMarker;
import org.eclipse.linuxtools.internal.rpm.rpmlint.RpmlintLog;
import org.eclipse.linuxtools.rpm.ui.editor.SpecfileEditor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMarkerResolution2;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

/**
 * Base class providing common functionality for rpmlint resolution.
 *
 */
public abstract class ARpmlintResolution implements IMarkerResolution2 {


    /**
     * No image for rpmlint resolutions for now.
     *
     * @see org.eclipse.ui.IMarkerResolution2#getImage()
     */
    @Override
    public Image getImage() {
        return null;
    }

    /**
     * Returns the SpecfileEditor for the given IMarker if any.
     *
     * @param marker The marker to use for retrieving the editor.
     * @return The SpecfileEditor this marker is from or null if it's not a SpecfileEditor.
     */
    protected SpecfileEditor getEditor(IMarker marker) {
        // Open or activate the editor.
        IWorkbenchPage page = PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow().getActivePage();
        IEditorPart part;
        try {
            part = IDE.openEditor(page, marker);
        } catch (PartInitException e) {
            RpmlintLog.logError(e);
            return null;
        }
        // Get the editor's document.
        if (!(part instanceof SpecfileEditor)) {
            return null;
        }
        return (SpecfileEditor) part;
    }
}
