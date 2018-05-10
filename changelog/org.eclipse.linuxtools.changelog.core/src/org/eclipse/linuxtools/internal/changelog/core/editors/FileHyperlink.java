/*******************************************************************************
 * Copyright (c) 2006, 2018 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Kyu Lee <klee@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.changelog.core.editors;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

/**
 * Hyperlink that opens up editor for a file.
 */
public class FileHyperlink implements IHyperlink {

    private IFile fileLoc;

    private IRegion region;

    public FileHyperlink(IRegion regionIn, IFile fileIn) {
        fileLoc = fileIn;
        region = regionIn;
    }

    @Override
    public IRegion getHyperlinkRegion() {
        return region;
    }

    @Override
    public String getTypeLabel() {
        return null;
    }

    @Override
    public String getHyperlinkText() {
        return null;
    }

    /**
     * Opens the hyperlink in new editor window.
     */
    @Override
    public void open() {
        IWorkbench ws = PlatformUI.getWorkbench();
        try {
            org.eclipse.ui.ide.IDE.openEditor(ws.getActiveWorkbenchWindow()
                    .getActivePage(), fileLoc, true);
        } catch (PartInitException e) {
            e.printStackTrace();

        }

    }
}
