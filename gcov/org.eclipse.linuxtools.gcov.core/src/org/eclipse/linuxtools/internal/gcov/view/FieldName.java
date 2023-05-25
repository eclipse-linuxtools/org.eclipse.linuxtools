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

import org.eclipse.linuxtools.dataviewers.abstractviewers.AbstractSTDataViewersField;
import org.eclipse.linuxtools.internal.gcov.model.TreeElement;

public class FieldName extends AbstractSTDataViewersField {

    @Override
    public String getColumnHeaderText() {
        return Messages.FieldName_column_header;
    }

    @Override
    public String getValue(Object obj) {
        if (obj instanceof TreeElement e) {
            String nm = e.getName();
            nm = nm.substring(nm.lastIndexOf('/') + 1);
            nm = nm.substring(nm.lastIndexOf('\\') + 1);
            return nm;
        }
        return ""; //$NON-NLS-1$
    }

    @Override
    public String getToolTipText(Object element) {
        if (element instanceof TreeElement elem) {
            return elem.getName();
        }
        return ""; //$NON-NLS-1$
    }

    @Override
    public int compare(Object obj1, Object obj2) {
        String s1 = getValue(obj1);
        String s2 = getValue(obj2);
        if (s1 == null) {
            if (s2 == null) {
                return 0;
            }
            return -1;
        }
        if (s2 == null) {
            return 1;
        }
        return s1.compareTo(s2);
    }
}
