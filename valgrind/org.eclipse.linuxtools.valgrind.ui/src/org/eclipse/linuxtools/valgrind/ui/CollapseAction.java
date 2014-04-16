/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.valgrind.ui;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.linuxtools.internal.valgrind.ui.Messages;

/**
 * Action used for providing collapsing functionality to TreeViewer.
 * @since 2.0
 *
 */
public class CollapseAction extends Action {

    private TreeViewer viewer;

    /**
     * Create the action for the particular TreeViewer.
     *
     * @param viewer The viewer to collapse.
     */
    public CollapseAction(TreeViewer viewer) {
        super(Messages.getString("CollapseAction.Text")); //$NON-NLS-1$
        this.viewer = viewer;
    }

    @Override
    public void run() {
        IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
        Object element = selection.getFirstElement();
        viewer.collapseToLevel(element, AbstractTreeViewer.ALL_LEVELS);
    }

}
