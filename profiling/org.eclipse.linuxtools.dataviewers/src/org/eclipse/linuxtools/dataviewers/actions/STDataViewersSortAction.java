/*******************************************************************************
 * Copyright (c) 2009 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marzia Maugeri <marzia.maugeri@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.dataviewers.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.window.Window;
import org.eclipse.linuxtools.dataviewers.abstractviewers.AbstractSTViewer;
import org.eclipse.linuxtools.dataviewers.abstractviewers.STDataViewersImages;
import org.eclipse.linuxtools.dataviewers.abstractviewers.STDataViewersMessages;
import org.eclipse.linuxtools.dataviewers.dialogs.STDataViewersSortDialog;
import org.eclipse.swt.custom.BusyIndicator;

/**
 * This action allows the user to sort the data in the viewer
 */
public class STDataViewersSortAction extends Action {

    private final AbstractSTViewer stViewer;

    private final STDataViewersSortDialog dialog;

    /**
     * Creates the action for the given viewer.
     *
     * @param stViewer The AbstractSTViewer to create the action for.
     */
    public STDataViewersSortAction(AbstractSTViewer stViewer) {
		super(STDataViewersMessages.sortAction_title, STDataViewersImages
				.getImageDescriptor(STDataViewersImages.IMG_SORT));
		super.setToolTipText(STDataViewersMessages.sortAction_tooltip);
		this.stViewer = stViewer;

		// building a sort dialog
		dialog = new STDataViewersSortDialog(stViewer.getViewer().getControl().getShell(), stViewer.getTableSorter());

		setEnabled(true);
    }

    @Override
	public void run() {
        if (dialog.open() == Window.OK && dialog.isDirty()) {
            BusyIndicator.showWhile(null, new Runnable() {
                @Override
				public void run() {
                    stViewer.setComparator(dialog.getSorter());
                }
            });

        }
    }
}