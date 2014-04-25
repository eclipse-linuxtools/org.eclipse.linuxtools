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
package org.eclipse.linuxtools.man.views;

import org.eclipse.linuxtools.internal.man.parser.ManDocument;
import org.eclipse.linuxtools.internal.man.views.ManTextViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

/**
 * View for man pages.
 */

public class ManView extends ViewPart {

    /**
     * The ID of the view as specified by the extension.
     */
    public static final String ID = "org.eclipse.linuxtools.man.views.ManView"; //$NON-NLS-1$

    private ManTextViewer viewer;

    /**
     * This is a callback that will allow us to create the viewer and initialize
     * it.
     */
    @Override
    public void createPartControl(Composite parent) {
        viewer = new ManTextViewer(parent);
        // Create the help context id for the viewer's control
        PlatformUI
                .getWorkbench()
                .getHelpSystem()
                .setHelp(viewer.getControl(),
                        "org.eclipse.linuxtools.man.viewer"); //$NON-NLS-1$
    }

    /**
     * Sets the man page to dispaly
     *
     * @param manPageName
     *            The name of the man page to display.
     */
    public void setManPageName(String manPageName) {
        viewer.setDocument(new ManDocument(manPageName));
    }

    /**
     * Passing the focus request to the viewer's control.
     */
    @Override
    public void setFocus() {
        viewer.getControl().setFocus();
    }
}