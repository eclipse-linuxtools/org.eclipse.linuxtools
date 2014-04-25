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

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.linuxtools.internal.rpm.ui.editor.SpecfileLog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

/**
 * Hyperlink implementation for the source and patch files in a srpm. Note: This
 * implementation assumes two filesystem layouts where it looks for files. 1.
 * Exploder srpm - Spec file and sources in one directory. 2. Rpmbuild structure
 * - Assumes that the edited spec file is in a SPECS folder and looks for
 * sources in ../SOURCES.
 */
public class SourcesFileHyperlink implements IHyperlink {

    private String fileName;
    private IFile original;
    private IRegion region;

    /**
     * Creates hyperlink for the following file name, region and file whether
     * the file name is found.
     *
     * @param original The file where the reference to this file name is.
     * @param fileName The name of the file to open.
     * @param region The hyperlink region.
     */
    public SourcesFileHyperlink(IFile original, String fileName, IRegion region) {
        this.fileName = fileName;
        this.original = original;
        this.region = region;
    }

    /**
     * @see org.eclipse.jface.text.hyperlink.IHyperlink#getHyperlinkRegion()
     */
    @Override
    public IRegion getHyperlinkRegion() {
        return region;
    }

    /**
     * @see org.eclipse.jface.text.hyperlink.IHyperlink#getHyperlinkText()
     */
    @Override
    public String getHyperlinkText() {
        return NLS.bind(Messages.SourcesFileHyperlink_0, fileName);
    }

    /**
     * @see org.eclipse.jface.text.hyperlink.IHyperlink#getTypeLabel()
     */
    @Override
    public String getTypeLabel() {
        return null;
    }

    /**
     * Tries to open the given file name looking for it in the current directory
     * and in ../SOURCES.
     *
     * @see org.eclipse.jface.text.hyperlink.IHyperlink#open()
     */
    @Override
    public void open() {
        IContainer container = original.getParent();
        IResource resourceToOpen = container.findMember(fileName);
        if (resourceToOpen == null) {
            IResource sourcesFolder = container.getParent().findMember(
                    "SOURCES"); //$NON-NLS-1$
            resourceToOpen = ((IFolder) sourcesFolder).getFile(fileName);
        }
        IWorkbenchPage page = PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow().getActivePage();
        try {
            if (resourceToOpen.getType() == IResource.FILE) {
                IDE.openEditor(page, (IFile) resourceToOpen);
            }
        } catch (PartInitException e) {
            SpecfileLog.logError(e);
        }
    }
}