/*******************************************************************************
 * Copyright (c) 2009, 2018 STMicroelectronics and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marzia Maugeri <marzia.maugeri@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.dataviewers.actions;

import java.util.Iterator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ResourceLocator;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.linuxtools.dataviewers.STDataViewersActivator;
import org.eclipse.linuxtools.dataviewers.abstractviewers.AbstractSTTreeViewer;
import org.eclipse.linuxtools.dataviewers.abstractviewers.STDataViewersMessages;

/**
 * This action collapse the selected items of the tree
 *
 */
public class STCollapseSelectionAction extends Action {

    private final AbstractSTTreeViewer stViewer;

    /**
     * Constructor
     *
     * @param stViewer
     *            the stViewer to collapse
     */
    public STCollapseSelectionAction(AbstractSTTreeViewer stViewer) {
		super(STDataViewersMessages.collapseSelectionAction_title, ResourceLocator
				.imageDescriptorFromBundle(STDataViewersActivator.PLUGIN_ID, "icons/collapse_all.gif").get()); //$NON-NLS-1$
        this.stViewer = stViewer;
    }

    @Override
    public void run() {
        IStructuredSelection selection = stViewer.getViewer().getStructuredSelection();
        if (selection != null && selection != TreeSelection.EMPTY) {
            for (Iterator<?> itSel = selection.iterator(); itSel.hasNext();) {
                stViewer.getViewer().collapseToLevel(itSel.next(), AbstractTreeViewer.ALL_LEVELS);
            }
        }
    }
}
