/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/

package org.eclipse.cdt.oprofile.core;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

import org.eclipse.cdt.oprofile.core.opxml.DefaultsProcessor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;


/**
 * A class to hold generic information about Oprofile.
 * @author Keith Seitz <keiths@redhat.com>
 */
public class OpInfo {
	// Oprofile defaults
	public static final String DEFAULT_SAMPLE_DIR = DefaultsProcessor.SAMPLE_DIR;
	public static final String DEFAULT_LOCK_FILE = DefaultsProcessor.LOCK_FILE;
	public static final String DEFAULT_LOG_FILE = DefaultsProcessor.LOG_FILE;
	public static final String DEFAULT_DUMP_STATUS = DefaultsProcessor.DUMP_STATUS;
	
	// A comparator class used when sorting events
	// (sorting by event name)
	private static class SortEventComparator implements Comparator {
		public int compare(Object a, Object b) {
			OpEvent event1 = (OpEvent) a;
			OpEvent event2 = (OpEvent) b;
			return event1.getText().compareTo(event2.getText());
		}
	}

	// A comparator class used when searching events
	// (searching by event name)
	private static class SearchEventComparator implements Comparator {
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
	private HashMap _defaults;
	
	// A list of all the events for each counter (temporary)
	private ArrayList[] _eventArrayList = null;
	
	// The permanent list of events indexed by counter
	private OpEvent[][] _eventList;
	
	// The sample directory in use
	private String _dir;
	
	// The CPU frequency of this CPU in MHz
	private double _cpuSpeed;
	
	/**
	 * Return all of Oprofile's generic information.
	 * @return a class containing the information
	 */
	public static OpInfo getInfo() {		
		// Run opmxl and get the static information
		OpInfo info = new OpInfo();
		if (Oprofile.isKernelModuleLoaded()) {
			try {
				IRunnableWithProgress opxml = OprofileCorePlugin.getDefault().getOpxmlProvider().info(info);
				opxml.run(null);
				info._init();
			} catch (InvocationTargetException e) {
			} catch (InterruptedException e) {
			} catch (OpxmlException e) {
				String title = OprofileProperties.getString("opxmlProvider.error.dialog.title"); //$NON-NLS-1$
				String msg = OprofileProperties.getString("opxmlProvider.error.dialog.message"); //$NON-NLS-1$
				ErrorDialog.openError(null /* parent shell */, title, msg, e.getStatus());
			}
		}
		
		return info;
	}
	
	// Initializes internal state
	private void _init() {
		_dir = getDefault(DEFAULT_SAMPLE_DIR);
		
		_eventList = new OpEvent[getNrCounters()][];
		for (int i = 0; i < getNrCounters(); ++i) {
			_eventList[i] = new OpEvent[_eventArrayList[i].size()];
			_eventArrayList[i].toArray(_eventList[i]);
			Arrays.sort(_eventList[i], new SortEventComparator());
		}
		
		// Done with arraylists
		_eventArrayList = null;
	}
	
	/**
	 * Returns the sample directory in use
	 * @return the directory
	 */
	public String getDir() {
		return _dir;
	}
	
	/**
	 * Sets the sample directory to use
	 * @param dir the sample directory
	 */
	public void setDir(String dir) {
		_dir = dir;
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
	 * after this object is contstructed, but after opxml is run.
	 * @param ctrs the number of counters
	 */
	public void setNrCounters(int ctrs) {
		_nrCounters = ctrs;
		
		// Allocate room for event lists for the counters
		_eventArrayList = new ArrayList[ctrs];
		for (int i = 0; i < ctrs; ++i) {
			_eventArrayList[i] = new ArrayList();
		}
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
	 * Adds an event into the list of events allowed for the given counter.
	 * @param num the counter for the event
	 * @param event the event
	 */
	public void setEvent (int num, OpEvent event) {
		if (num < _eventArrayList.length) {
			_eventArrayList[num].add(event);
		}
	}
	
	/**
	 * Returns an array of events valid for the given counter number.
	 * (-1 for all counters) 
	 * @param num the counter number
	 * @return an array of valid events; <bold>never</bold> returns <code>null</code>.
	 */ 
	public OpEvent[] getEvents(int num) {
		if (num < _eventList.length)
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
	public void setDefaults(HashMap map) {
		_defaults = map;
	}
}
