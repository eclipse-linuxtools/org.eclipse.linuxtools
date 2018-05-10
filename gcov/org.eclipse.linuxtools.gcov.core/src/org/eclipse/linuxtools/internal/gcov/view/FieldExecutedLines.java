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
 *    Xavier Raynaud <xavier.raynaud@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.gcov.view;

import java.text.NumberFormat;

import org.eclipse.linuxtools.dataviewers.abstractviewers.AbstractSTDataViewersField;
import org.eclipse.linuxtools.dataviewers.charts.provider.IChartField;
import org.eclipse.linuxtools.internal.gcov.model.TreeElement;

public class FieldExecutedLines extends AbstractSTDataViewersField implements IChartField {

    @Override
    public String getColumnHeaderText() {
        return Messages.FieldExecutedLines_column_header;
    }

    @Override
    public String getValue(Object obj) {
        int v = getExecutedLines(obj);
        return NumberFormat.getInstance().format(v);
    }

    @Override
    public String getToolTipText(Object element) {
        int v = getExecutedLines(element);
        String s = NumberFormat.getInstance().format(v);
        s += Messages.FieldExecutedLines_column_tooltip;
        if (v > 1) {
            s += "s"; //$NON-NLS-1$
        }
        return s;
    }

    @Override
    public int compare(Object obj1, Object obj2) {
        int i1 = getExecutedLines(obj1);
        int i2 = getExecutedLines(obj2);
        if (i1 > i2) {
            return 1;
        }
        if (i1 < i2) {
            return -1;
        }
        return 0;
    }

    private int getExecutedLines(Object o) {
        if (o instanceof TreeElement) {
            return ((TreeElement) o).getExecutedLines();
        }
        return 0;
    }

    @Override
    public Integer getNumber(Object obj) {
        return getExecutedLines(obj);
    }
}
