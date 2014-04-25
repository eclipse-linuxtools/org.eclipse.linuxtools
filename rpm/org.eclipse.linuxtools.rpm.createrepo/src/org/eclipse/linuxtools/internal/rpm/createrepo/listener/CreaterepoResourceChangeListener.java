/*******************************************************************************
 * Copyright (c) 2013 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Neil Guzman - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.createrepo.listener;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.linuxtools.internal.rpm.createrepo.Activator;
import org.eclipse.linuxtools.internal.rpm.createrepo.CreaterepoProject;
import org.eclipse.linuxtools.internal.rpm.createrepo.Messages;
import org.eclipse.linuxtools.internal.rpm.createrepo.form.RepoFormEditor;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.ResourceUtil;

/**
 * Handle the closing of the editors when their projects
 * get deleted or closed.
 */
public class CreaterepoResourceChangeListener implements
        IResourceChangeListener {

    private CreaterepoProject project;

    /** Default Constructor. */
    public CreaterepoResourceChangeListener(CreaterepoProject project) {
        this.project = project;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
     */
    @Override
    public void resourceChanged(IResourceChangeEvent event) {
        switch (event.getType()) {
        case IResourceChangeEvent.POST_CHANGE:
        case IResourceChangeEvent.PRE_CLOSE:
        case IResourceChangeEvent.PRE_DELETE:
            PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
                @Override
                public void run() {
                    closeEditors();
                }
            });
            break;
        }
    }

    /**
     * Close the editors thats resource has been affected by a change in the
     * workspace. E.g. if an editor's project has been closed/deleted, close the
     * editor.
     */
    private void closeEditors() {
        IWorkbench workbench = PlatformUI.getWorkbench();
        IResource repomdFile = project.getRepoFile();
        // If the file is gone, that could mean: the project is closed, the project was
        // deleted, or the file was deleted. If so, close the editor of the file.
        if (!repomdFile.exists()) {
            for (IWorkbenchPage page : workbench.getActiveWorkbenchWindow().getPages()) {
                for (IEditorReference ref : page.getEditorReferences()) {
                    try {
                        // get the resource from the editor part and exit the editor if
                        // it is within the project(s) being closed/deleted
                        IResource resource = ResourceUtil.getResource(ref.getEditorInput());
                        if (ref.getId().equals(RepoFormEditor.EDITOR_ID) &&
                                resource.getProject().equals(project.getProject())) {
                            page.closeEditor(ref.getEditor(false), false);
                        }
                    } catch (PartInitException e) {
                        Activator.logError(Messages.CreaterepoResourceChangeListener_errorGettingResource, e);
                    }
                }
            }
        }
    }

}
