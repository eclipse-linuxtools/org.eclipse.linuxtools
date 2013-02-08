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
import org.eclipse.linuxtools.dataviewers.charts.provider.IChartField;
import org.eclipse.linuxtools.internal.gprof.view.GmonView;
import org.eclipse.linuxtools.internal.gprof.view.histogram.CGArc;
import org.eclipse.linuxtools.internal.gprof.view.histogram.CGCategory;
import org.eclipse.linuxtools.internal.gprof.view.histogram.HistFunction;
import org.eclipse.linuxtools.internal.gprof.view.histogram.HistRoot;
import org.eclipse.linuxtools.internal.gprof.view.histogram.TreeElement;
import org.eclipse.swt.graphics.Color;


/**
 * Column "calls" of displayed elements
 *
 * @author Xavier Raynaud <xavier.raynaud@st.com>
 */
public class CallsProfField extends AbstractSTDataViewersField implements IChartField{

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.dataviewers.abstractviewers.ISTDataViewersField#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(Object obj1, Object obj2) {
		TreeElement e1 = (TreeElement) obj1;
		TreeElement e2 = (TreeElement) obj2;
		int s1 = e1.getCalls();
		int s2 = e2.getCalls();
		return s1 - s2;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.dataviewers.abstractviewers.ISTDataViewersField#getColumnHeaderText()
	 */
	@Override
	public String getColumnHeaderText() {
		return "Calls";
	}
	
	

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.dataviewers.abstractviewers.AbstractSTDataViewersField#getColumnHeaderTooltip()
	 */
	@Override
	public String getColumnHeaderTooltip() {
		return null;
	}
	
	

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.dataviewers.abstractviewers.AbstractSTDataViewersField#getToolTipText(java.lang.Object)
	 */
	@Override
	public String getToolTipText(Object element) {
		if (element instanceof HistRoot) {
			return "total number of function call";
		} else if (element instanceof HistFunction) {
			return "number of times the function \"" + ((HistFunction)element).getName() + "\" was invoked";
		} else if (element instanceof CGCategory) {
			CGCategory cat = (CGCategory) element;
			if (CGCategory.CHILDREN.equals(cat.getName())) {
				return "number of function call performed by the function \"" + cat.getParent().getName() + "\"";
			} else {
				return "number of times the function \"" + cat.getParent().getName() + "\" was invoked";
			}
		} else if (element instanceof CGArc) {
			CGArc cgarc = (CGArc) element;
			if (CGCategory.CHILDREN.equals(cgarc.getParent().getName())) {
				return "number of times the function \"" +
						cgarc.getParent().getParent().getName() +
						"\" called the function \"" +
						cgarc.getFunctionName() + "\"";
			} else {
				return "number of times the function \"" +
						cgarc.getFunctionName() + 
						"\" called the function \"" +
						cgarc.getParent().getParent().getName() + "\"";
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.dataviewers.abstractviewers.ISTDataViewersField#getValue(java.lang.Object)
	 */
	@Override
	public String getValue(Object obj) {
		TreeElement e = (TreeElement) obj;
		int i = e.getCalls();
		if (i == -1) return ""; //$NON-NLS-1$
		String ret = String.valueOf(i);
		return ret;
	}
	

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.dataviewers.abstractviewers.AbstractSTDataViewersField#getBackground(java.lang.Object)
	 */
	@Override
	public Color getBackground(Object element) {
		return GmonView.getBackground(element);
	}

	@Override
	public Number getNumber(Object obj) {
		TreeElement e = (TreeElement) obj;
		int i = e.getCalls();
		if (i == -1) return 0L;
		return i;
	}

}
