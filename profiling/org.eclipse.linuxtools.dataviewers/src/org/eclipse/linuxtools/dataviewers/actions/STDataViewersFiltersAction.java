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
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.linuxtools.dataviewers.abstractviewers.STDataViewersImages;
import org.eclipse.linuxtools.dataviewers.abstractviewers.STDataViewersMessages;
import org.eclipse.swt.graphics.Image;


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
