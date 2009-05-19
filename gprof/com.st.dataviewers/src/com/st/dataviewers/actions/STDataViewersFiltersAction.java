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
import org.eclipse.swt.graphics.Image;

import com.st.dataviewers.abstractviewers.STDataViewersImages;
import com.st.dataviewers.abstractviewers.STDataViewersMessages;

public class STDataViewersFiltersAction extends Action {

    /**
     * Creates the action
     */
    public STDataViewersFiltersAction() {
        super(STDataViewersMessages.filtersAction_title);
        Image img = STDataViewersImages.getImage(STDataViewersImages.IMG_FILTER); 
        super.setImageDescriptor(ImageDescriptor.createFromImage(img));
        super.setToolTipText(STDataViewersMessages.filtersAction_tooltip);
        super.setEnabled(true);
    }
}
