/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.views.BrowserView;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;


/**
 * This <code>Action</code> expands or collapses the Viewer to the level of the element that the
 * user selected.
 * @author Henry Hughes
 * @author Ryan Morse
 */
public class TreeExpandCollapseAction extends Action implements
        ISelectionListener {
    private final IWorkbenchWindow fWindow;
    private IStructuredSelection selection;
    private final BrowserView viewer;

    /**
     * The default constructor. Takes a <code>Class</code> representing the viewer that it is to expand
     * or collapse, as there is only one in the workbench at a time.
     * @param cls    <code>Class</code> of the viewer to expand/collapse
     */
    public TreeExpandCollapseAction(BrowserView view) {
        super();
        fWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        fWindow.getSelectionService().addSelectionListener(this);
        this.viewer = view;
    }

    /**
     * Updates <code>selection</code> with the current selection whenever the user changes
     * the current selection.
     */
    @Override
    public void selectionChanged(IWorkbenchPart part, ISelection incoming) {
        if (incoming instanceof IStructuredSelection) {
            selection = (IStructuredSelection) incoming;
            setEnabled(selection.size() == 1);
        } else {
            // Other selections, for example containing text or of other kinds.
            setEnabled(false);
        }
    }

    public void dispose() {
        fWindow.getSelectionService().removeSelectionListener(this);
    }

    /**
     * The main body of the action. Expands or Collapses the viewer specified at construction to
     * the level of the current selection.
     */
    @Override
    public void run() {
        ISelection incoming = viewer.getViewer().getSelection();
        IStructuredSelection selection = (IStructuredSelection)incoming;
        Object o = selection.getFirstElement();

        if(o == null) {
            return;
        }

        Object[] objs = viewer.getViewer().getVisibleExpandedElements();
        boolean doExpand = true;

        for(Object obj : objs) {
            if(obj == o) {
                doExpand = false;
            }
        }

        if(doExpand) {
            viewer.getViewer().expandToLevel(o,1);
        } else {
            viewer.getViewer().collapseToLevel(o,1);
        }
    }
}
