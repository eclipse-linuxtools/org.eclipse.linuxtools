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
import org.eclipse.linuxtools.dataviewers.abstractviewers.AbstractSTTreeViewer;
import org.eclipse.linuxtools.dataviewers.charts.provider.IChartField;
import org.eclipse.linuxtools.internal.gprof.parser.GmonDecoder;
import org.eclipse.linuxtools.internal.gprof.parser.HistogramDecoder;
import org.eclipse.linuxtools.internal.gprof.view.GmonView;
import org.eclipse.linuxtools.internal.gprof.view.histogram.HistFile;
import org.eclipse.linuxtools.internal.gprof.view.histogram.HistFunction;
import org.eclipse.linuxtools.internal.gprof.view.histogram.HistLine;
import org.eclipse.linuxtools.internal.gprof.view.histogram.HistRoot;
import org.eclipse.linuxtools.internal.gprof.view.histogram.TreeElement;
import org.eclipse.swt.graphics.Color;


/**
 * Column "Samples" of displayed elements
 *
 * @author Xavier Raynaud <xavier.raynaud@st.com>
 */
public class SampleProfField extends AbstractSTDataViewersField implements IChartField{

	private boolean samples = true;
	protected final AbstractSTTreeViewer viewer;
	protected final static double UNINITIALIZED = 0;
	
	/**
	 * Constructor
	 * @param viewer the gmon viewer
	 */
	public SampleProfField(AbstractSTTreeViewer viewer) {
		this.viewer = viewer;
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
		if (samples) return "Samples";
		return "Time";
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
	 * @see org.eclipse.linuxtools.dataviewers.abstractviewers.ISTDataViewersField#getValue(java.lang.Object)
	 */
	@Override
	public String getValue(Object obj) {
		TreeElement e = (TreeElement) obj;
		int i = e.getSamples();
		if (i == -1) return ""; //$NON-NLS-1$
		if (samples) {
			return String.valueOf(i);
		} else {
			double prof_rate = getProfRate();
			if (prof_rate == UNINITIALIZED) return "?"; //$NON-NLS-1$
			return getValue(i, prof_rate);
		}
	}
	
	/**
	 * Get the time value with the best unit display
	 * @param i nbr of samples
	 * @param prof_rate profiling frequency
	 * @return time value with the best adapted time unit
	 */
	public static String getValue(double i, double prof_rate)
	{
		long timeInNs = (long) (i/prof_rate);
		long ns = timeInNs%1000;
		
		long timeInUs = timeInNs/1000;
		if (timeInUs == 0) return ns + "ns";
		long us = timeInUs%1000;
		
		long timeInMs = timeInUs/1000;
		if (timeInMs == 0) {
			String ns_s = "" + ns;
			while (ns_s.length() < 3) ns_s = "0" + ns_s;
			return us + "." + ns_s + "us";
		}
		long ms = timeInMs%1000;
		
		long timeInS = timeInMs/1000;
		if (timeInS == 0) {
			String us_s = "" + us;
			while (us_s.length() < 3) us_s = "0" + us_s;
			return ms + "." + us_s + "ms";
		}
		long s = timeInS%60;
		
		long timeInMin = timeInS/60;
		if (timeInMin == 0) {
			String ms_s = "" + ms;
			while (ms_s.length() < 3) ms_s = "0" + ms_s;
			return s + "." + ms_s + "s";
		}
		long min = timeInMin%60;
		
		long timeInHour = timeInMin/60;
		if (timeInHour == 0) return min + "min " + s + "s";
		
		return timeInHour + "h " + min + "min";
	}
	

	protected double getProfRate() {
		double prof_rate = UNINITIALIZED;
		Object o = viewer.getViewer().getInput();
		if (o instanceof GmonDecoder) {
			GmonDecoder decoder = (GmonDecoder)  o;
			HistogramDecoder histo = decoder.getHistogramDecoder();
			prof_rate = histo.getProf_rate();
			char tUnit = histo.getTimeDimension();
			switch (tUnit) {
			case 's': prof_rate /= 1000000000; break;
			case 'm': prof_rate /= 1000000; break;
			case 'u': prof_rate /= 1000; break;
			}
		}
		return prof_rate;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.dataviewers.abstractviewers.AbstractSTDataViewersField#getBackground(java.lang.Object)
	 */
	@Override
	public Color getBackground(Object element) {
		return GmonView.getBackground(element);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.dataviewers.abstractviewers.AbstractSTDataViewersField#getToolTipText(java.lang.Object)
	 */
	@Override
	public String getToolTipText(Object element) {
		if (element instanceof HistRoot) {
			return "total time spent in the program";
		} else if (element instanceof HistFunction) {
			return "time spent in this function";
		} else if (element instanceof HistFile) {
			return "time spent in this file";
		} else if (element instanceof HistLine) {
			return "time spent at this location";
		}
		return null;
	}

	/**
	 * Switch from samples to time ans vice-versa
	 */
	public void toggle() {
		this.samples = !this.samples;
	}

	@Override
	public Number getNumber(Object obj) {
		TreeElement e = (TreeElement) obj;
		int i = e.getSamples();
		if (i == -1) return 0L;
		return i;
	}

}
