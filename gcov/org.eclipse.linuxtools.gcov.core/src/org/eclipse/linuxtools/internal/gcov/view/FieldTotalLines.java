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
import org.eclipse.linuxtools.internal.gcov.model.CovFileTreeElement;
import org.eclipse.linuxtools.internal.gcov.model.CovFunctionTreeElement;
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

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.dataviewers.abstractviewers.ISTDataViewersField#getValue(java.lang.Object)
	 */
	@Override
	public String getValue(Object obj) {	
		TreeElement e = (TreeElement) obj;
		if (e.getClass() != CovFunctionTreeElement.class)
			return Integer.toString(e.getTotalLines());
		else 
			return ""; //$NON-NLS-1$
	}


	/*
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.dataviewers.abstractviewers.AbstractSTDataViewersField#getToolTipText(java.lang.Object)
	 */
	@Override
	public String getToolTipText(Object element) {
		TreeElement e = (TreeElement) element;
		if (e.getClass() != CovFunctionTreeElement.class)
			return "Total lines number = "+Integer.toString(e.getTotalLines());
		else 
			return ""; //$NON-NLS-1$
	}


	/*
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.dataviewers.abstractviewers.ISTDataViewersField#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(Object obj1, Object obj2) {
		TreeElement e1 = (TreeElement) obj1;
		TreeElement e2 = (TreeElement) obj2;

		if (e1.getClass() == CovFileTreeElement.class) {
			String s1 = Integer.toString(((CovFileTreeElement)e1).getTotalLines());
			String s2 = Integer.toString(((CovFileTreeElement)e2).getTotalLines());
			if (s1 == null) {
				if (s2 == null)
					return 0;
				return -1;
			}
			if (s2 == null)
				return 1;
			return s1.compareTo(s2);
		}
		else return 0;
	}
}
