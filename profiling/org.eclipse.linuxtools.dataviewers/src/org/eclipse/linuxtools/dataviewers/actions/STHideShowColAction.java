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
import org.eclipse.jface.window.Window;
import org.eclipse.linuxtools.dataviewers.STDataViewersActivator;
import org.eclipse.linuxtools.dataviewers.abstractviewers.AbstractSTViewer;
import org.eclipse.linuxtools.dataviewers.abstractviewers.STDataViewersMessages;
import org.eclipse.linuxtools.dataviewers.dialogs.STDataViewersHideShowColumnsDialog;

/**
 * This action allows the user to hide/show some columns.
 */
public class STHideShowColAction extends Action {

    private final AbstractSTViewer stViewer;

    /**
     * Creates the action for the given viewer.
     *
     * @param stViewer The AbstractSTViewer to create the action for.
     */
	public STHideShowColAction(AbstractSTViewer stViewer) {
		super(STDataViewersMessages.hideshowAction_title, ResourceLocator
				.imageDescriptorFromBundle(STDataViewersActivator.PLUGIN_ID, "icons/prop_edt.gif").get()); //$NON-NLS-1$
		this.stViewer = stViewer;
		setEnabled(true);
    }

    @Override
    public void run() {
        STDataViewersHideShowColumnsDialog dialog = new STDataViewersHideShowColumnsDialog(stViewer);

        if (dialog.open() == Window.OK && dialog.isDirty()) {
            if (dialog.getManager() != null) {
                stViewer.setHideShowManager(dialog.getManager());
            }
        }
    }
}
