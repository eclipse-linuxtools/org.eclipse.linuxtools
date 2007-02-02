/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/
package org.eclipse.cdt.oprofile.core.opxml;

import org.eclipse.cdt.oprofile.core.OpInfo;
import org.xml.sax.Attributes;


/**
 * Opxml processor for the "info" command.
 * @see org.eclipse.cdt.oprofile.core.opxml.OpxmlRunner
 * @author Keith Seitz  <keiths@redhat.com>
 */
public class OpInfoProcessor extends XMLProcessor {
	// Other XMLProcessors used by this processor
	private DefaultsProcessor _defaultsProc;
	private EventListProcessor _eventListProc;
	
	// XML tags processed by this processor
	public static final String NUM_COUNTERS_TAG = "num-counters"; //$NON-NLS-1$
	public static final String DEFAULTS_TAG = "defaults"; //$NON-NLS-1$
	public static final String EVENT_LIST_TAG = "event-list"; //$NON-NLS-1$
	public static final String CPU_FREQUENCY_TAG = "cpu-frequency"; //$NON-NLS-1$

	public OpInfoProcessor() {
		_defaultsProc = new DefaultsProcessor();
		_eventListProc = new EventListProcessor();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.oprofile.core.opxml.XMLProcessor#startElement(java.lang.String, org.xml.sax.Attributes, java.lang.Object)
	 */
	public void startElement(String name, Attributes attrs, Object callData) {
		if (name.equals(DEFAULTS_TAG)) {
			OprofileSAXHandler.getInstance(callData).push(_defaultsProc);
		} else if (name.equals(EVENT_LIST_TAG)) {
			OprofileSAXHandler.getInstance(callData).push(_eventListProc);
			_eventListProc.startElement(name, attrs, callData);
		} else {
			super.startElement(name, attrs, callData);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.oprofile.core.opxml.XMLProcessor#endElement(java.lang.String, java.lang.Object)
	 */
	public void endElement(String name, Object callData) {
		if (name.equals(CPU_FREQUENCY_TAG)) {
			double speed = Double.parseDouble(_characters);
			OpInfo info = (OpInfo) callData;
			info.setCPUSpeed(speed);
		} else if (name.endsWith(NUM_COUNTERS_TAG)) {
			int numCounters = 0;
			try {
				numCounters = Integer.parseInt(_characters);
			} catch (NumberFormatException nfe) {
			}
			OpInfo info = (OpInfo) callData;
			info.setNrCounters(numCounters);
		}
	}
	
}
