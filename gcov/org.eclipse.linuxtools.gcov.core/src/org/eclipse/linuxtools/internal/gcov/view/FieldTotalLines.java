/*******************************************************************************
 * Copyright (c) 2009 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Xavier Raynaud <xavier.raynaud@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.gcov.view;

import java.text.NumberFormat;

import org.eclipse.linuxtools.dataviewers.abstractviewers.AbstractSTDataViewersField;
import org.eclipse.linuxtools.internal.gcov.model.TreeElement;
import org.eclipse.osgi.util.NLS;

public class FieldTotalLines extends AbstractSTDataViewersField {

	@Override
    public String getColumnHeaderText() {
        return Messages.FieldTotalLines_column_header;
    }

    private int getTotalLines(Object element) {
        if (element instanceof TreeElement) {
            return ((TreeElement) element).getTotalLines();
        }
        return -1;
    }

    @Override
    public String getValue(Object obj) {
        int v = getTotalLines(obj);
        if (v < 0) {
            return ""; //$NON-NLS-1$
        }
        return NumberFormat.getInstance().format(v);
    }

    @Override
    public String getToolTipText(Object element) {
        int v = getTotalLines(element);
        if (v < 0) {
            return null;
        }
        String s = NumberFormat.getInstance().format(v);
        String message;
        if (v > 1) {
            message = NLS.bind(Messages.FieldTotalLines_column_tooltip_1, s);
        } else {
            message = NLS.bind(Messages.FieldTotalLines_column_tooltip_0, s);
        }
        return message;
    }

    @Override
    public int compare(Object obj1, Object obj2) {
        int i1 = getTotalLines(obj1);
        int i2 = getTotalLines(obj2);
        if (i1 > i2) {
            return 1;
        }
        if (i1 < i2) {
            return -1;
        }
        return 0;
    }
}
