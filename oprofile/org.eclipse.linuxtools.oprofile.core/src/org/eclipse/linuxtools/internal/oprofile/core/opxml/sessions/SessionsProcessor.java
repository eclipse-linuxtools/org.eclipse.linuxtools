/*******************************************************************************
 * Copyright (c) 2004 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Keith Seitz <keiths@redhat.com> - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.linuxtools.internal.oprofile.core.opxml.sessions;

import java.util.ArrayList;

import org.eclipse.linuxtools.internal.oprofile.core.model.OpModelEvent;
import org.eclipse.linuxtools.internal.oprofile.core.model.OpModelSession;
import org.eclipse.linuxtools.internal.oprofile.core.opxml.XMLProcessor;
import org.xml.sax.Attributes;


/**
 * A processor for sessions.
 * @see org.eclipse.linuxtools.internal.oprofile.core.opxml.OpxmlRunner
 */
public class SessionsProcessor extends XMLProcessor {
	public static class SessionInfo {
		/**
		 *  A list of Session as well as Events under each of them
		 */
		public ArrayList<OpModelSession> list;

		public SessionInfo(ArrayList<OpModelSession> session){
			this.list = session;
		}
	}
	
	// XML tags recognized by this processor
	public static final String SESSION_TAG = "session"; //$NON-NLS-1$
	private static final String SESSION_NAME_ATTR = "name"; //$NON-NLS-1$
	public static final String SAMPLE_COUNT_TAG = "count"; //$NON-NLS-1$
	public static final String EVENT_TAG = "event"; //$NON-NLS-1$
	private static final String EVENT_NAME_ATTR = "name"; //$NON-NLS-1$
	
	/**
	 *  The current session being constructed
	 */
	private OpModelSession currentSession;
	
	/**
	 *  The current event being constructed
	 */
	private OpModelEvent currentEvent;
	
	/**
	 *  A list of all sessions
	 */
	private ArrayList<OpModelEvent> eventList;

	/**
	 * @see org.eclipse.linuxtools.internal.oprofile.core.opxml.XMLProcessor#startElement(java.lang.String, org.xml.sax.Attributes, java.lang.Object)
	 */
	@Override
	public void startElement(String name, Attributes attrs, Object callData) {
		if (name.equals(SESSION_TAG)) {
			String sessionName = valid_string(attrs.getValue(SESSION_NAME_ATTR));
			currentSession = new OpModelSession(sessionName);
			eventList = new ArrayList<>();
		} else if (name.equals(EVENT_TAG)) {
			String eventName = attrs.getValue(EVENT_NAME_ATTR);
			currentEvent = new OpModelEvent(currentSession,eventName);
		} else {
			super.startElement(name, attrs, callData);
		}
	}
	
	/**
	 * @see org.eclipse.linuxtools.internal.oprofile.core.opxml.XMLProcessor#endElement(java.lang.String, java.lang.Object)
	 */
	@Override
	public void endElement(String name, Object callData) {
		if (name.equals(SESSION_TAG)) {
			// Got end of session -- save in session list
			OpModelEvent[] s = new OpModelEvent[eventList.size()];
			eventList.toArray(s);
			currentSession.setEvents(s);
			SessionInfo info = (SessionInfo) callData;
			info.list.add(currentSession);
			currentSession = null;
			eventList = null;
		} else if (name.equals(EVENT_TAG)) {
			// Got end of event -- save session list into current OpModelEvent and
			// save current event into call data
			eventList.add(currentEvent);
			currentEvent = null;
		} else {
			super.endElement(name, callData);
		}
	}
}
