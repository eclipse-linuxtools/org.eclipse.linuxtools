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
import org.eclipse.linuxtools.internal.gprof.view.GmonView;
import org.eclipse.linuxtools.internal.gprof.view.histogram.TreeElement;
import org.eclipse.swt.graphics.Color;


/**
 * Column "name", or "location" of the displayed element
 *
 * @author Xavier Raynaud <xavier.raynaud@st.com>
 */
public class NameProfField extends AbstractSTDataViewersField {

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.dataviewers.abstractviewers.ISTDataViewersField#compare(java.lang.Object, java.lang.Object)
	 */
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

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.dataviewers.abstractviewers.ISTDataViewersField#getColumnHeaderText()
	 */
	public String getColumnHeaderText() {
		return "Name (location)";
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.dataviewers.abstractviewers.ISTDataViewersField#getValue(java.lang.Object)
	 */
	public String getValue(Object obj) {
		TreeElement e = (TreeElement) obj;
		return e.getName();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.dataviewers.abstractviewers.AbstractSTDataViewersField#getBackground(java.lang.Object)
	 */
	public Color getBackground(Object element) {
		return GmonView.getBackground(element);
	}

	@Override
	public String getToolTipText(Object element) {
		TreeElement elem = (TreeElement) element;
		String s = elem.getSourcePath();
		if (s != null && !"??".equals(s)) {
			int lineNumber = elem.getSourceLine();
			if (lineNumber > 0) {
				return s + ":" + lineNumber;
			}
			return s;
		}
		return null;
	}

}
