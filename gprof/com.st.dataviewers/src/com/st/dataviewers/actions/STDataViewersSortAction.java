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
package com.st.dataviewers.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Image;
import com.st.dataviewers.abstractviewers.AbstractSTViewer;
import com.st.dataviewers.abstractviewers.STDataViewersImages;
import com.st.dataviewers.abstractviewers.STDataViewersMessages;
import com.st.dataviewers.dialogs.STDataViewersSortDialog;

/**
 * This action allows the user to sort the data in the viewer
 */
public class STDataViewersSortAction extends Action {
    
	private final AbstractSTViewer stViewer;
    
    private final STDataViewersSortDialog dialog;

    /**
     * Constructor
     * @param view
     * @param dialog
     */
    public STDataViewersSortAction(AbstractSTViewer stViewer) {
        super(STDataViewersMessages.sortAction_title);
        Image img = STDataViewersImages.getImage(STDataViewersImages.IMG_SORT); 
        super.setImageDescriptor(ImageDescriptor.createFromImage(img));
        super.setToolTipText(STDataViewersMessages.sortAction_tooltip);
        this.stViewer = stViewer;
        
        //building a sort dialog 
        dialog = getSortDialog();

        setEnabled(true);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.action.Action#run()
     */
    public void run() {
        if (dialog.open() == Window.OK && dialog.isDirty()) {
        	BusyIndicator.showWhile(null,new Runnable() {
    			public void run() {
    				stViewer.setComparator(dialog.getSorter());
    			}
    		});
        	
        }
    }
    
    /**
	 * Return a sort dialog for the receiver.
	 * 
	 * @return TableSortDialog
	 */
	protected STDataViewersSortDialog getSortDialog() {
		return new STDataViewersSortDialog(stViewer.getViewer().getControl().getShell(),stViewer.getTableSorter());
	}
}