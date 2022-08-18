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
 *    Red Hat, Inc. - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.changelog.core.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.linuxtools.internal.changelog.core.Messages;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.ui.synchronize.ISynchronizeModelElement;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

public class PrepareChangelogKeyHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) {

        IStructuredSelection tempResult = null;

        // try getting currently selected project
            IWorkbenchPage ref = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();
            IWorkbenchPart part = HandlerUtil.getActivePart(event);
            if (part instanceof IEditorPart editorPart) {
                // If we are in an editor, check if the file being edited is an IResource
                // that belongs to a project in the workspace
                IEditorInput input = editorPart.getEditorInput();
                IResource r = input.getAdapter(IResource.class);
                if (r != null) {
                    // We have an IResource to work with, so create a selection we can use
                    // in PrepareChangeLogAction
                    tempResult = new StructuredSelection(r);
                }
            } else {
                // Otherwise, our view is not an editor, see if we have an IResource or something
                // that will lead us to an IResource
                ISelection selected = ref.getSelection();
                if (selected instanceof IStructuredSelection iss) {
                    IResource r = null;
                    Object o = iss.getFirstElement();
                    if (o instanceof ISynchronizeModelElement s) {
                        r = s.getResource();
                    } else if (o instanceof IAdaptable a) {
                        r = a.getAdapter(IResource.class);
                    }
                    if (r != null)
                        tempResult = iss;
                }
            }
            if (tempResult == null) {
                // We don't have an obvious project match in the current active view.
                // Let's search all open views for the Synchronize View which is our first
                // choice to fall back on.
                for (IViewReference view: ref.getViewReferences()) {
                    if (view.getId().equals("org.eclipse.team.sync.views.SynchronizeView")) { // $NON-NLS-1$
                        IViewPart v = view.getView(false);
                        ISelection s = null;
                        ISelectionProvider sp = v.getViewSite().getSelectionProvider();
                        if (sp != null) {
                            s = sp.getSelection();
                        }
                        if (s instanceof IStructuredSelection ss) {
                            Object element = ss.getFirstElement();
                            IResource r = null;
                            if (element instanceof ISynchronizeModelElement syncE) {
                                r = syncE.getResource();
                            } else if (element instanceof IAdaptable a) {
                                r = a.getAdapter(IResource.class);
                            }

                            if (r != null) {
                                tempResult = ss;
                            }
                        }
                    }
                }
            }

        // If we can't find the project directly, let the user know.
        if (tempResult == null) {
            MessageDialog.openInformation(getActiveWorkbenchShell(), Messages.getString("ChangeLog.PrepareChangeLog"), // $NON-NLS-1$,
                    Messages.getString("PrepareChangeLog.InfoNoProjectFound")); // $NON-NLS-1$
            return null;
        }

        final IStructuredSelection result = tempResult;
        IAction exampleAction = new PrepareChangeLogAction() {
            @Override
            public void run() {
                setSelection(result);
                doRun();
            }
        };

        exampleAction.run();

        return null;
    }

    /**
     * Returns active shell.
     */
    private Shell getActiveWorkbenchShell() {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window != null) {
            return window.getShell();
        }
        return null;
    }
}
