/*******************************************************************************
 * Copyright (c) 2009 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Xavier Raynaud <xavier.raynaud@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.gprof.view.fields;

import org.eclipse.linuxtools.dataviewers.abstractviewers.AbstractSTDataViewersField;
import org.eclipse.linuxtools.internal.gprof.Messages;
import org.eclipse.linuxtools.internal.gprof.view.GmonView;
import org.eclipse.linuxtools.internal.gprof.view.histogram.TreeElement;
import org.eclipse.swt.graphics.Color;

/**
 * Column "name", or "location" of the displayed element
 *
 * @author Xavier Raynaud <xavier.raynaud@st.com>
 */
public class NameProfField extends AbstractSTDataViewersField {

    @Override
    public int compare(Object obj1, Object obj2) {
        TreeElement e1 = (TreeElement) obj1;
        TreeElement e2 = (TreeElement) obj2;
        String s1 = e1.getName();
        String s2 = e2.getName();
        if (s1 == null) {
            if (s2 == null) return 0;
            return -1;
        }
        if (s2 == null) return 1;
        return s1.compareTo(s2);
    }

    @Override
    public String getColumnHeaderText() {
        return Messages.NameProfField_NAME_AND_LOCATION;
    }

    @Override
    public String getValue(Object obj) {
        if (obj instanceof TreeElement) {
            TreeElement e = (TreeElement) obj;
            return e.getName();
        }
        return ""; //$NON-NLS-1$
    }

    @Override
    public Color getBackground(Object element) {
        return GmonView.getBackground(element);
    }

    @Override
    public String getToolTipText(Object element) {
        TreeElement elem = (TreeElement) element;
        String s = elem.getSourcePath();
        if (s != null && !"??".equals(s)) { //$NON-NLS-1$
            int lineNumber = elem.getSourceLine();
            if (lineNumber > 0) {
                return s + ":" + lineNumber; //$NON-NLS-1$
            }
            return s;
        }
        return null;
    }

    @Override
    public int getPreferredWidth() {
        return 250;
    }

}
