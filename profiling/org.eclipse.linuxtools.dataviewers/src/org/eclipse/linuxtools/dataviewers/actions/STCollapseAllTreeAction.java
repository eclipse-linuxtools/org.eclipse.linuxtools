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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ResourceLocator;
import org.eclipse.linuxtools.dataviewers.STDataViewersActivator;
import org.eclipse.linuxtools.dataviewers.abstractviewers.AbstractSTTreeViewer;
import org.eclipse.linuxtools.dataviewers.abstractviewers.STDataViewersMessages;

/**
 * This action collapse all the tree
 *
 */
public class STCollapseAllTreeAction extends Action {

    private final AbstractSTTreeViewer stViewer;

    /**
     * Constructor
     *
     * @param stViewer
     *            the stViewer to collapse
     */
    public STCollapseAllTreeAction(AbstractSTTreeViewer stViewer) {
		super(STDataViewersMessages.collapseAllAction_title, ResourceLocator
				.imageDescriptorFromBundle(STDataViewersActivator.PLUGIN_ID, "icons/collapse_all.gif").get()); //$NON-NLS-1$
		this.stViewer = stViewer;
    }

    @Override
    public void run() {
        Object input = stViewer.getViewer().getInput();
        if (input != null) {
            stViewer.getViewer().collapseAll();
        }
    }
}
