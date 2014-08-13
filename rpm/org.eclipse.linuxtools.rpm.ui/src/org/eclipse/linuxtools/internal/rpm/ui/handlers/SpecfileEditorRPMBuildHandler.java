/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.linuxtools.internal.rpm.ui.Activator;
import org.eclipse.linuxtools.internal.rpm.ui.Messages;
import org.eclipse.linuxtools.rpm.core.IRPMConstants;
import org.eclipse.linuxtools.rpm.core.RPMProject;
import org.eclipse.linuxtools.rpm.core.RPMProjectLayout;
import org.eclipse.linuxtools.rpm.ui.RPMExportOperation;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.EditorPart;

/**
 * Common functionality of all build handlers.
 *
 * @since 1.0.0
 */
public class SpecfileEditorRPMBuildHandler extends AbstractHandler {

    private static final String BUILD_TYPE = "buildType"; //$NON-NLS-1$
    private static final String ON_EDITOR = "actOnEditor"; //$NON-NLS-1$
    protected RPMProject rpj;

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        final IResource resource = getResource(event);
        rpj = getRPMProject(resource);
        if (rpj != null) {
            Job job = new RPMExportOperation(rpj, event.getParameter(BUILD_TYPE));
            job.setUser(true);
            job.schedule();
        }
        return null;
    }

    /**
     * Extract the IResource that was selected when the event was fired.
     * @param event The fired execution event.
     * @return The resource that was selected.
     */
    private static IResource getResource(ExecutionEvent event) {
        final boolean actOnEditor = Boolean.valueOf(event.getParameter(ON_EDITOR));
        if (actOnEditor) {
            IEditorPart epart = HandlerUtil.getActiveEditor(event);
            if (epart != null) {
                IEditorInput input = epart.getEditorInput();
                if (input instanceof IFileEditorInput) {
                    return ((IFileEditorInput) input).getFile();
                }
            }
            return null;
        }
        IWorkbenchPart part = HandlerUtil.getActivePart(event);
        if (part == null) {
            return null;
        }
        if (part instanceof EditorPart) {
            IEditorInput input = ((EditorPart) part).getEditorInput();
            if (input instanceof IFileEditorInput) {
                return ((IFileEditorInput) input).getFile();
            }
            return null;
        }
        IWorkbenchSite site = part.getSite();
        if (site == null) {
            return null;
        }
        ISelectionProvider provider = site.getSelectionProvider();
        if (provider == null) {
            return null;
        }
        ISelection selection = provider.getSelection();
        if (selection instanceof IStructuredSelection) {
            Object element = ((IStructuredSelection) selection)
                    .getFirstElement();
            if (element instanceof IResource) {
                return (IResource) element;
            } else if (element instanceof IAdaptable) {
                IAdaptable adaptable = (IAdaptable) element;
                return (IResource) adaptable.getAdapter(IResource.class);
            } else {
                return null;
            }
        }
        return null;
    }

    /**
     * Get an RPMProject for the resource creating a new instance to an RPMProject.
     * @param resource The resource to check its parent project.
     * @return The RPMProject of the resource passed in.
     */
    private static RPMProject getRPMProject(IResource resource) {
        if (resource != null) {
            try {
                IProject parentProject = resource.getProject();

                // determine if project selected is an RPMProject
                if (parentProject.hasNature(IRPMConstants.RPM_NATURE_ID)) {
                    if (parentProject.getPersistentProperty(new QualifiedName(IRPMConstants.RPM_CORE_ID, IRPMConstants.SPECS_FOLDER)) != null){
                        return new RPMProject(parentProject, RPMProjectLayout.RPMBUILD);
                    } else {
                        return new RPMProject(parentProject, RPMProjectLayout.FLAT);
                    }
                }
            } catch (CoreException e) {
                Activator.logError(Messages.getString("SpecfileEditorRPMBuildHandler.logRPMProjectError"), e); //$NON-NLS-1$
            }
        }
        return null;
    }
}
