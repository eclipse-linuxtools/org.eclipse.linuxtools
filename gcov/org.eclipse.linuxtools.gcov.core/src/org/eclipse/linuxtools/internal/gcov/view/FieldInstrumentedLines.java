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

import org.eclipse.linuxtools.dataviewers.abstractviewers.AbstractSTDataViewersField;
import org.eclipse.linuxtools.dataviewers.charts.provider.IChartField;
import org.eclipse.linuxtools.internal.gcov.model.TreeElement;



public class FieldInstrumentedLines extends AbstractSTDataViewersField implements IChartField{

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.dataviewers.abstractviewers.ISTDataViewersField#getColumnHeaderText()
	 */
	public String getColumnHeaderText() {
		return "Instrumented Lines";
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.dataviewers.abstractviewers.ISTDataViewersField#getValue(java.lang.Object)
	 */
	public String getValue(Object obj) {
		TreeElement e = (TreeElement) obj;
		return Integer.toString(e.getInstrumentedLines());
	}


	/*
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.dataviewers.abstractviewers.AbstractSTDataViewersField#getToolTipText(java.lang.Object)
	 */
	@Override
	public String getToolTipText(Object element) {
		TreeElement e = (TreeElement) element;
		String s = "Instrumented lines number = "+Integer.toString(e.getInstrumentedLines());
		return s;
	}


	/*
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.dataviewers.abstractviewers.ISTDataViewersField#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Object obj1, Object obj2) {
		TreeElement e1 = (TreeElement) obj1;
		TreeElement e2 = (TreeElement) obj2;
		String s1 = Integer.toString(e1.getInstrumentedLines());
		String s2 = Integer.toString(e2.getInstrumentedLines());
		if (s1 == null) {
			if (s2 == null) return 0;
			return -1;
		}
		if (s2 == null) return 1;
		return s1.compareTo(s2);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.dataviewers.charts.provider.IChartField#getNumber(java.lang.Object)
	 */
	public Number getNumber(Object obj) {
		TreeElement e = (TreeElement) obj;
		return e.getInstrumentedLines();
	}

}
