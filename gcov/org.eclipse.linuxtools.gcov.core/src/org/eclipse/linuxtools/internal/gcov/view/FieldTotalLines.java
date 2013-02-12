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



public class FieldTotalLines extends AbstractSTDataViewersField {
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.dataviewers.abstractviewers.ISTDataViewersField#getColumnHeaderText()
	 */
	@Override
	public String getColumnHeaderText() {
		return "Total Lines";
	}

	private int getTotalLines(Object element) {
	    if (element instanceof TreeElement) {
	        return ((TreeElement) element).getTotalLines();
	    }
	    return -1;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.dataviewers.abstractviewers.ISTDataViewersField#getValue(java.lang.Object)
	 */
	@Override
	public String getValue(Object obj) {
		int v = getTotalLines(obj);
		if (v < 0) return ""; //$NON-NLS-1$
		return NumberFormat.getInstance().format(v);
	}


	/*
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.dataviewers.abstractviewers.AbstractSTDataViewersField#getToolTipText(java.lang.Object)
	 */
	@Override
	public String getToolTipText(Object element) {
	    int v = getTotalLines(element);
	    if (v < 0) return null;
	    String s = NumberFormat.getInstance().format(v);
	    s += " line";
	    if (v>1) s+= "s";
	    s += " in total";
	    return s;
	}


	/*
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.dataviewers.abstractviewers.ISTDataViewersField#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(Object obj1, Object obj2) {
		int i1 = getTotalLines(obj1);
		int i2 = getTotalLines(obj2);
		if (i1>i2) return 1;
		if (i1<i2) return -1;
		return 0;
	}
}
