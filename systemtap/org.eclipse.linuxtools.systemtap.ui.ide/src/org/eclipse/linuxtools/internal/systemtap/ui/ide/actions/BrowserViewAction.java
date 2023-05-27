/*******************************************************************************
 * Copyright (c) 2014, 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.views.BrowserView;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

public abstract class BrowserViewAction extends Action implements ISelectionListener, IDoubleClickListener {
    private final IWorkbenchWindow window;
    private final BrowserView viewer;
    private IStructuredSelection selection;
    private TreeExpandCollapseAction expandAction;

    /**
     * The Default Constructor. Takes the <code>IWorkbenchWindow</code> that it effects
     * as well as the <code>BrowserView</code> that will fire this action.
     * @param window    window effected by this event
     * @param browser    browser that fires this action
     */
    public BrowserViewAction(IWorkbenchWindow window, BrowserView browser) {
        this.window = window;
        window.getSelectionService().addSelectionListener(this);
        viewer = browser;
        expandAction = new TreeExpandCollapseAction(viewer);
    }

    public void dispose() {
        window.getSelectionService().removeSelectionListener(this);
        selection = null;
        expandAction.dispose();
        expandAction = null;
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

    @Override
    public void doubleClick(DoubleClickEvent event) {
        run();
    }

    protected Object getSelectedElement() {
        return viewer.getViewer().getStructuredSelection().getFirstElement();
    }

    protected void runExpandAction() {
        expandAction.run();
    }
}