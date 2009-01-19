/*******************************************************************************
 * Copyright (c) 2004, 2008, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Keith Seitz <keiths@redhat.com> - initial API and implementation
 *    Kent Sebastian <ksebasti@redhat.com>
 *******************************************************************************/ 

package org.eclipse.linuxtools.oprofile.core.daemon;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.linuxtools.oprofile.core.OprofileCorePlugin;
import org.eclipse.linuxtools.oprofile.core.OpxmlException;
import org.eclipse.linuxtools.oprofile.core.opxml.info.DefaultsProcessor;


/**
 * A class to hold generic information about Oprofile.
 */
public class OpInfo {
	// Oprofile defaults
	public static final String DEFAULT_SAMPLE_DIR = DefaultsProcessor.SAMPLE_DIR;
	public static final String DEFAULT_LOCK_FILE = DefaultsProcessor.LOCK_FILE;
	public static final String DEFAULT_LOG_FILE = DefaultsProcessor.LOG_FILE;
	public static final String DEFAULT_DUMP_STATUS = DefaultsProcessor.DUMP_STATUS;
	
	// A comparator class used when sorting events
	// (sorting by event name)
	private static class SortEventComparator implements Comparator<OpEvent> {
		public int compare(OpEvent o1, OpEvent o2) {
			return o1.getText().compareTo(o2.getText());
		}
	}

	// A comparator class used when searching events
	// (searching by event name)
	private static class SearchEventComparator implements Comparator<Object> {
		public int compare(Object a, Object b) {
			String astr, bstr;
			if (a instanceof String) {
				astr = (String) a;
				bstr = ((OpEvent) b).getText();
			} else {
				astr = ((OpEvent) a).getText();
				bstr = (String) b;
			}
			return astr.compareTo(bstr);
		}
	}
	
	// The number of counters supported by this configuration
	private int _nrCounters;
	
	// A HashMap of Oprofile defaults
	private HashMap<String,String> _defaults;
	
	// The permanent list of events indexed by counter
	private OpEvent[][] _eventList;
	
	// The CPU frequency of this CPU in MHz
	private double _cpuSpeed;
	
	/**
	 * Return all of Oprofile's generic information.
	 * @return a class containing the information
	 */
	public static OpInfo getInfo() {		
		// Run opmxl and get the static information
		OpInfo info = new OpInfo();

		try {
			IRunnableWithProgress opxml = OprofileCorePlugin.getDefault().getOpxmlProvider().info(info);
			opxml.run(null);
		} catch (InvocationTargetException e) {
		} catch (InterruptedException e) {
		} catch (OpxmlException e) {
			OprofileCorePlugin.showErrorDialog("opxmlProvider", e); //$NON-NLS-1$
		}
		
		return info;
	}
	
	/**
	 * Returns the number of counters allowed by Oprofile
	 * @return the number of counters
	 */
	public int getNrCounters() {
		return _nrCounters;
	}
	
	/**
	 * Sets the number of counters allowed by Oprofile. This method is called
	 * after this object is contstructed, while opxml is run (the first tag output
	 * is num-counters).
	 * @param ctrs the number of counters
	 */
	public void setNrCounters(int ctrs) {
		_nrCounters = ctrs;
		
		// Allocate room for event lists for the counters
		_eventList = new OpEvent[_nrCounters][];
	}
	
	/**
	 * Set the CPU frequency (in MHz)
	 * @param freq the frequency
	 */
	public void setCPUSpeed(double freq) {
		_cpuSpeed = freq;
	}
	
	/**
	 * Returns the CPU's speed in MHz
	 * @return the speed
	 */
	public double getCPUSpeed() {
		return _cpuSpeed;
	}
	
	/**
	 * Returns the requested default. Valid defaults are <code>DEFAULT_DUMP_STATUS</code>,
	 * <code>DEFAULT_LOCK_FILE</code>, <code>DEFAULT_LOG_FILE</code>, and
	 * <code>DEFAULT_SAMPLE_DIR</code>.
	 * @param what which default to return
	 * @return the requested default or <code>null</code> if not known
	 */
	public String getDefault(String what) {
		return (String) _defaults.get(what);
	}

	/**
	 * Adds the events of the counter counterNum into the list of all events.
	 * Note they are sorted here.
	 * @param counterNum the counter with the events
	 * @param events an array of OpEvent events belonging to this counter
	 */
	public void setEvents(int counterNum, OpEvent[] events) {
		if (counterNum < _eventList.length) {
			_eventList[counterNum] = events;
			Arrays.sort(_eventList[counterNum], new SortEventComparator());
		}
	}

	/**
	 * Returns an array of events valid for the given counter number.
	 * @param num the counter number
	 * @return an array of valid events
	 */ 
	public OpEvent[] getEvents(int num) {
		if (num >= 0 && num < _eventList.length)
			return _eventList[num];
		
		return new OpEvent[0];
	}
	
	/**
	 * Searches the for the event with the given name
	 * @param name the name of the event (e.g., CPU_CLK_UNHALTED)
	 * @return the event or <code>null</code> if not found
	 */
	public OpEvent findEvent(String name) {
		// Search through all counters
		for (int counter = 0; counter < getNrCounters(); ++counter) {
			int idx = Arrays.binarySearch(getEvents(counter), name, new SearchEventComparator());
			if (idx >= 0)
				return _eventList[counter][idx];
		}
		
		return null;
	}
	
	/**
	 * Sets the defaults associated with this configuration of Oprofile.
	 * @param map the <code>HashMap</code> containing the defaults
	 */
	public void setDefaults(HashMap<String,String> map) {
		_defaults = map;
	}
}
