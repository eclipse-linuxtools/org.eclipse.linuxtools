/*******************************************************************************
 * Copyright (c) 2009 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Xavier Raynaud <xavier.raynaud@st.com> - initial API and implementation
 *   Mohamed Korbosli <mohamed.korbosli@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.gprof.view.fields;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.eclipse.linuxtools.dataviewers.abstractviewers.AbstractPercentageDrawerField;
import org.eclipse.linuxtools.dataviewers.charts.provider.IChartField;
import org.eclipse.linuxtools.internal.gprof.Messages;
import org.eclipse.linuxtools.internal.gprof.view.histogram.TreeElement;


/**
 * Column "sample ratio" of the displayed element
 *
 * @author Mohamed Korbosli
 */
public class RatioProfField extends AbstractPercentageDrawerField implements IChartField{
	
	/** Format to use to display percentages */
	public final static NumberFormat nf = new DecimalFormat("##0.0#"); //$NON-NLS-1$
	
	
	/**
	 * Gets the percentage value to display
	 * @param obj
	 * @return the percentage value to display, as a float
	 */
	@Override
	public float getPercentage(Object obj) {
		TreeElement e = (TreeElement) obj;
		int SamplesSum = e.getRoot().getSamples();
		if (SamplesSum == 0) return 0;
		else return ((100.0f*e.getSamples())/e.getRoot().getSamples());
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.dataviewers.abstractviewers.ISTDataViewersField#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(Object obj1, Object obj2) {
		TreeElement e1 = (TreeElement) obj1;
		TreeElement e2 = (TreeElement) obj2;
		int s1 = e1.getSamples();
		int s2 = e2.getSamples();
		return s1 - s2;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.dataviewers.abstractviewers.ISTDataViewersField#getColumnHeaderText()
	 */
	@Override
	public String getColumnHeaderText() {
		return Messages.RatioProfField_TIME_PERCENTAGE;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.dataviewers.abstractviewers.AbstractPercentageDrawerField#getNumberFormat()
	 */
	@Override
	public NumberFormat getNumberFormat() {
		return nf;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.dataviewers.abstractviewers.AbstractPercentageDrawerField#isSettedNumberFormat()
	 */
	@Override
	public boolean isSettedNumberFormat() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.dataviewers.charts.provider.IChartField#getNumber(java.lang.Object)
	 */
	@Override
	public Number getNumber(Object obj) {
		float f = getPercentage(obj);
		return new Float(f);
	}

}
