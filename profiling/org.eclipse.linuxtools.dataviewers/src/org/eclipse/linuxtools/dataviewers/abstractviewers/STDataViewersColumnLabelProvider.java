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
package org.eclipse.linuxtools.dataviewers.abstractviewers;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.linuxtools.dataviewers.STDataViewersActivator;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Item;

/**
 * Label provider, on top of {@link ISTDataViewersField}s
 */
public class STDataViewersColumnLabelProvider extends ColumnLabelProvider {
    private final ISTDataViewersField fields;

    /**
     * Create a STDataViewersColumnLabelProvider on a field
     *
     * @param column
     */
    public STDataViewersColumnLabelProvider(Item column) {
        Object data = column.getData();
        if (data instanceof ISTDataViewersField) {
            fields = (ISTDataViewersField) data;
        } else {
            STDataViewersActivator
                    .getDefault()
                    .getLog()
                    .log(new Status(IStatus.ERROR, STDataViewersActivator.PLUGIN_ID,
                            "No ISTDataField associated to Column!"));
            fields = null;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.viewers.ColumnLabelProvider#getText(java.lang.Object)
     */
    @Override
	public String getText(Object element) {
        return fields.getValue(element);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.viewers.ColumnLabelProvider#getImage(java.lang.Object)
     */
    @Override
	public Image getImage(Object element) {
        return fields.getImage(element);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.viewers.CellLabelProvider#getToolTipText(java.lang.Object)
     */
    @Override
    public String getToolTipText(Object element) {
        return fields.getToolTipText(element);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.viewers.ColumnLabelProvider#getBackground(java.lang.Object)
     */
    @Override
    public Color getBackground(Object element) {
        return fields.getBackground(element);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.viewers.ColumnLabelProvider#getForeground(java.lang.Object)
     */
    @Override
    public Color getForeground(Object element) {
        return fields.getForeground(element);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.viewers.CellLabelProvider#useNativeToolTip(java.lang.Object)
     */
    @Override
	public boolean useNativeToolTip(Object object) {
        return true;
    }

}
