/*******************************************************************************
 * Copyright (c) 2004,2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Keith Seitz <keiths@redhat.com> - initial API and implementation
 *    Kent Sebastian <ksebasti@redhat.com>
 *******************************************************************************/ 
package org.eclipse.linuxtools.internal.oprofile.core.opxml.info;

import org.eclipse.linuxtools.internal.oprofile.core.daemon.OpInfo;
import org.eclipse.linuxtools.internal.oprofile.core.opxml.OprofileSAXHandler;
import org.eclipse.linuxtools.internal.oprofile.core.opxml.XMLProcessor;
import org.xml.sax.Attributes;


/**
 * Opxml processor for the "info" command.
 * @see org.eclipse.linuxtools.internal.oprofile.core.opxml.OpxmlRunner
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
	public static final String TIMER_MODE = "timer-mode";  //$NON-NLS-1$

	public OpInfoProcessor() {
		_defaultsProc = new DefaultsProcessor();
		_eventListProc = new EventListProcessor();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.internal.oprofile.core.opxml.XMLProcessor#startElement(java.lang.String, org.xml.sax.Attributes, java.lang.Object)
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
	 * @see org.eclipse.linuxtools.internal.oprofile.core.opxml.XMLProcessor#endElement(java.lang.String, java.lang.Object)
	 */
	public void endElement(String name, Object callData) {
		if (name.equals(CPU_FREQUENCY_TAG)) {
			double speed = Double.parseDouble(_characters);
			OpInfo info = (OpInfo) callData;
			info._setCPUSpeed(speed);
		} else if (name.equals(TIMER_MODE)) {
			boolean timerMode = Boolean.parseBoolean(_characters);
			OpInfo info = (OpInfo) callData;
			info._setTimerMode(timerMode);
		} else if (name.equals(NUM_COUNTERS_TAG)) {
			int numCounters = 0;
			try {
				numCounters = Integer.parseInt(_characters);
			} catch (NumberFormatException nfe) {
			}
			OpInfo info = (OpInfo) callData;
			info._setNrCounters(numCounters);
		} else if (name.equals(EVENT_LIST_TAG)) {
			OpInfo info = (OpInfo) callData;
			info._setEvents(_eventListProc.getCounterNum(), _eventListProc.getEvents());
		}
	}
}
