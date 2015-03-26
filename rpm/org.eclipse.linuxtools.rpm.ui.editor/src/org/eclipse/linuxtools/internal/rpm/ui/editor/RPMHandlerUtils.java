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
package org.eclipse.linuxtools.internal.rpm.ui.editor;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.linuxtools.internal.rpm.ui.editor.actions.Messages;
import org.eclipse.linuxtools.rpm.core.IRPMConstants;
import org.eclipse.linuxtools.rpm.core.RPMProject;
import org.eclipse.linuxtools.rpm.core.RPMProjectLayout;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.EditorPart;

/**
 * Utility class for RPM UI Editor Handler related things.
 *
 */
public class RPMHandlerUtils {

    /**
     * Utility classes should not have a public or default constructor.
     */
    private RPMHandlerUtils() {}

    /**
     * Extract the IResource that was selected when the event was fired.
     * @param event The fired execution event.
     * @return The resource that was selected.
     */
    public static IResource getResource(ExecutionEvent event) {
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
                return adaptable.getAdapter(IResource.class);
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
    public static RPMProject getRPMProject(IResource resource) {
        RPMProject rc = null;

        try {
            IProject parentProject = resource.getProject();

            // determine if project selected is an RPMProject
            if (parentProject.hasNature(IRPMConstants.RPM_NATURE_ID)) {
                if (parentProject.getPersistentProperty(new QualifiedName(IRPMConstants.RPM_CORE_ID, IRPMConstants.SPECS_FOLDER)) != null){
                    rc = new RPMProject(parentProject, RPMProjectLayout.RPMBUILD);
                } else {
                    rc = new RPMProject(parentProject, RPMProjectLayout.FLAT);
                }
            } else {
                rc = new RPMProject(parentProject, RPMProjectLayout.FLAT);
            }
        } catch (CoreException e) {
            SpecfileLog.logError(Messages.RPMHandlerUtils_cannotCreateRPMProject, e);
        }
        return rc;
    }
}
