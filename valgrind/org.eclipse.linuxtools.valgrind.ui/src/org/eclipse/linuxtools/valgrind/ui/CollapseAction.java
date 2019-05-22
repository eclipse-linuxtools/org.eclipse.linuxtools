/*******************************************************************************
 * Copyright (c) 2009, 2018 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
        IStructuredSelection selection = viewer.getStructuredSelection();
        Object element = selection.getFirstElement();
        viewer.collapseToLevel(element, AbstractTreeViewer.ALL_LEVELS);
    }

}
