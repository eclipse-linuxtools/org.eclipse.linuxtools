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

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.eclipse.linuxtools.dataviewers.abstractviewers.AbstractPercentageDrawerField;
import org.eclipse.linuxtools.dataviewers.charts.provider.IChartField;
import org.eclipse.linuxtools.internal.gcov.model.CovRootTreeElement;
import org.eclipse.linuxtools.internal.gcov.model.TreeElement;
import org.eclipse.osgi.util.NLS;

public class FieldCoveragePercentage extends AbstractPercentageDrawerField implements IChartField {

    public final static NumberFormat nf = new DecimalFormat("##0.0#"); //$NON-NLS-1$

    @Override
    public String getColumnHeaderText() {
        return Messages.FieldCoveragePercentage_column_header;
    }

    @Override
    public String getValue(Object obj) {
        float f = getPercentage(obj);
        if (f < 0) {
            f = 0.0f;
        }
        return nf.format(f);
    }

    /**
     * Gets the percentage value to display
     * @param obj
     * @return the percentage value to display, as a float
     */
    @Override
    public float getPercentage(Object obj) {
        TreeElement e = (TreeElement) obj;
        return e.getCoveragePercentage();
    }

    @Override
    public NumberFormat getNumberFormat() {
        return nf;
    }

    @Override
    public boolean isSettedNumberFormat() {
        return true;
    }

    @Override
    public String getToolTipText(Object element) {
        TreeElement e = (TreeElement) element;
        String s = NLS.bind(Messages.FieldCoveragePercentage_column_tooltip,
                Integer.toString((int) e.getCoveragePercentage()));
        return s;
    }

    @Override
    public int compare(Object obj1, Object obj2) {
        TreeElement e1 = (TreeElement) obj1;
        TreeElement e2 = (TreeElement) obj2;
        float f1 = e1.getCoveragePercentage();
        float f2 = e2.getCoveragePercentage();
        return Float.compare(f1, f2);
    }

    @Override
    public Number getNumber(Object obj) {
        TreeElement e = (TreeElement) obj;
        float f = getPercentage(obj);
        if (e.getClass() == CovRootTreeElement.class)
            return 0;
        else {
            if (f < 0) {
                f = 0.0f;
            }
            return f;
        }
    }
}
