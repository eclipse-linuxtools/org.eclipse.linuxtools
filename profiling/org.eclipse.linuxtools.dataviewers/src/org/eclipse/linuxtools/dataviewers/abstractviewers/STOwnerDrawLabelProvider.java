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
package org.eclipse.linuxtools.dataviewers.abstractviewers;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.OwnerDrawLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.linuxtools.dataviewers.STDataViewersActivator;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;

public class STOwnerDrawLabelProvider extends OwnerDrawLabelProvider {

    private final ISTDataViewersField fields;

    public STOwnerDrawLabelProvider(final Item column) {
        Object data = column.getData();

        if (data instanceof ISTDataViewersField) {
            fields = (ISTDataViewersField) data;
        } else {
			STDataViewersActivator.getDefault().getLog().log(Status.error("No ISTDataField associated to Column!"));
			fields = null;
        }
    }

    private String getText(Object element) {
        return fields.getValue(element);
    }

    private Color getBackground(Object element) {
        return fields.getBackground(element);
    }

    private Color getForeground(Object element) {
        return fields.getForeground(element);
    }

    private Image getImage(Object element) {
        return fields.getImage(element);
    }

    @Override
    public String getToolTipText(Object element) {
        return fields.getToolTipText(element);
    }

    @Override
    public boolean useNativeToolTip(Object object) {
        return true;
    }

    @Override
    protected void measure(Event event, Object element) {

    }

    @Override
    protected void paint(Event event, Object element) {
        if (fields.getSpecialDrawer(element) != null) {
            fields.getSpecialDrawer(element).handleEvent(event);
        }
    }

    @Override
    public void update(ViewerCell cell) {
        if (fields.getSpecialDrawer(cell.getElement()) == null) {
            Object element = cell.getElement();
            cell.setText(getText(element));
            cell.setImage(getImage(element));
            cell.setForeground(getForeground(element));
            cell.setBackground(getBackground(element));
        }
    }

    @Override
    protected void erase(Event event, Object element) {
    }

}
