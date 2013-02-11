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
import org.eclipse.linuxtools.dataviewers.charts.provider.IChartField;
import org.eclipse.linuxtools.internal.gcov.model.TreeElement;



public class FieldInstrumentedLines extends AbstractSTDataViewersField implements IChartField{

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.dataviewers.abstractviewers.ISTDataViewersField#getColumnHeaderText()
	 */
	@Override
	public String getColumnHeaderText() {
		return "Instrumented Lines";
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.dataviewers.abstractviewers.ISTDataViewersField#getValue(java.lang.Object)
	 */
	@Override
	public String getValue(Object obj) {
		int v = getInstrumentedLines(obj);
        return NumberFormat.getInstance().format(v);
	}


	/*
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.dataviewers.abstractviewers.AbstractSTDataViewersField#getToolTipText(java.lang.Object)
	 */
	@Override
	public String getToolTipText(Object element) {
	    int v = getInstrumentedLines(element);
        String s = NumberFormat.getInstance().format(v);
        s += " instrumented line";
        if (v > 1) s += "s";
		return s;
	}


	/*
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.dataviewers.abstractviewers.ISTDataViewersField#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(Object obj1, Object obj2) {
		int i1 = getInstrumentedLines(obj1);
		int i2 = getInstrumentedLines(obj2);
		if (i1>i2) return 1;
        if (i1<i2) return -1;
        return 0;
	}
    
    private int getInstrumentedLines(Object o) {
        if (o instanceof TreeElement) {
            return ((TreeElement) o).getInstrumentedLines();
        }
        return 0;
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.dataviewers.charts.provider.IChartField#getNumber(java.lang.Object)
	 */
	@Override
	public Integer getNumber(Object obj) {
		return getInstrumentedLines(obj);
	}

}
