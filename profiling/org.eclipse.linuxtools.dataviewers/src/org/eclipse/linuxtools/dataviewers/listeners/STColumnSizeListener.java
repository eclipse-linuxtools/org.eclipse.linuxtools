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
package org.eclipse.linuxtools.dataviewers.listeners;

import org.eclipse.linuxtools.dataviewers.abstractviewers.AbstractSTViewer;
import org.eclipse.linuxtools.dataviewers.abstractviewers.STDataViewersHideShowManager;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Listener;

public class STColumnSizeListener implements Listener {
    private STDataViewersHideShowManager hideshowMng;

    public STColumnSizeListener(STDataViewersHideShowManager hideshowMng) {
        this.hideshowMng = hideshowMng;
    }

    @Override
    public void handleEvent(Event event) {
        if (hideshowMng != null) {
            Item column = (Item) event.widget;
            AbstractSTViewer stViewer = hideshowMng.getSTViewer();
            int width = stViewer.getColumnWidth(column);
            hideshowMng.setWidth(stViewer.getColumnIndex(column), width);
        }
    }

}