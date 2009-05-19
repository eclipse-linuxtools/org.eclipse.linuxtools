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
package com.st.stgprof.view.fields;

import org.eclipse.swt.graphics.Color;

import com.st.dataviewers.abstractviewers.AbstractSTDataViewersField;
import com.st.dataviewers.abstractviewers.AbstractSTTreeViewer;
import com.st.stgprof.parser.GmonDecoder;
import com.st.stgprof.parser.HistogramDecoder;
import com.st.stgprof.view.GmonView;
import com.st.stgprof.view.histogram.HistFile;
import com.st.stgprof.view.histogram.HistFunction;
import com.st.stgprof.view.histogram.HistLine;
import com.st.stgprof.view.histogram.HistRoot;
import com.st.stgprof.view.histogram.TreeElement;

/**
 * Column "Samples" of displayed elements
 *
 * @author Xavier Raynaud <xavier.raynaud@st.com>
 */
public class SampleProfField extends AbstractSTDataViewersField {

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
	 * @see com.st.fp3.viewers.abstractview.ISTProfField#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Object obj1, Object obj2) {
		TreeElement e1 = (TreeElement) obj1;
		TreeElement e2 = (TreeElement) obj2;
		int s1 = e1.getSamples();
		int s2 = e2.getSamples();
		return s1 - s2;
	}

	/*
	 * (non-Javadoc)
	 * @see com.st.fp3.viewers.abstractview.ISTProfField#getColumnHeaderText()
	 */
	public String getColumnHeaderText() {
		String prefix = "";
		Object o = viewer.getInput();
		if (o instanceof GmonDecoder) {
			GmonDecoder decoder = (GmonDecoder) o;
			if (decoder.isICache()) {
				prefix = "ICACHE ";
			} else if (decoder.isDCache()) {
				prefix = "DCACHE ";
			}
		}
		if (samples) return prefix + "Samples";
		return prefix + "Time";
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.st.dataviewers.abstractviewers.AbstractSTDataViewersField#getColumnHeaderTooltip()
	 */
	public String getColumnHeaderTooltip() {
		Object o = viewer.getInput();
		if (o instanceof GmonDecoder) {
			GmonDecoder decoder = (GmonDecoder) o;
			if (decoder.isICache()) {
				return "Time spent by function accessing instruction cache";
			} else if (decoder.isDCache()) {
				return "Time spent by function accessing data cache";
			}
		}
		return null;
	}
	
	

	/*
	 * (non-Javadoc)
	 * @see com.st.fp3.viewers.abstractview.ISTProfField#getValue(java.lang.Object)
	 */
	public String getValue(Object obj) {
		TreeElement e = (TreeElement) obj;
		int i = e.getSamples();
		if (i == -1) return "";
		if (samples) {
			return String.valueOf(i);
		} else {
			double prof_rate = getProfRate();
			if (prof_rate == UNINITIALIZED) return "?";
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
		if (timeInMs == 0) return us + "." + ns + "us";
		long ms = timeInMs%1000;
		
		long timeInS = timeInMs/1000;
		if (timeInS == 0) return ms + "." + us + "ms";
		long s = timeInS%60;
		
		long timeInMin = timeInS/60;
		if (timeInMin == 0) return s + "." + ms + "s";
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
	 * @see com.st.fp3.viewers.abstractview.AbstractSTProfField#getBackground(java.lang.Object)
	 */
	public Color getBackground(Object element) {
		return GmonView.getBackground(element);
	}

	/* (non-Javadoc)
	 * @see com.st.fp3.viewers.abstractview.AbstractSTProfField#getToolTipText(java.lang.Object)
	 */
	@Override
	public String getToolTipText(Object element) {
		String suffix = "";
		Object o = viewer.getInput();
		if (o instanceof GmonDecoder) {
			GmonDecoder decoder = (GmonDecoder) o;
			if (decoder.isICache()) {
				suffix = " in instruction cache";
			} else if (decoder.isDCache()) {
				suffix = " in data cache";
			}
		}
		
		if (element instanceof HistRoot) {
			return "total time spent in the program" + suffix;
		} else if (element instanceof HistFunction) {
			return "time spent in this function" + suffix;
		} else if (element instanceof HistFile) {
			return "time spent in this file" + suffix;
		} else if (element instanceof HistLine) {
			return "time spent at this location" + suffix;
		}
		return null;
	}

	/**
	 * Switch from samples to time ans vice-versa
	 */
	public void toggle() {
		this.samples = !this.samples;
	}

	public Number getNumber(Object obj) {
		TreeElement e = (TreeElement) obj;
		int i = e.getSamples();
		if (i == -1) return 0L;
		return i;
	}

	
	
}
