/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/
package org.eclipse.cdt.oprofile.core.opxml;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.cdt.oprofile.core.OpInfo;
import org.eclipse.cdt.oprofile.core.SampleSession;
import org.eclipse.cdt.oprofile.core.SessionEvent;
import org.xml.sax.Attributes;


/**
 * A processor for sessions.
 * @see org.eclipse.cdt.oprofile.core.opxml.OpxmlRunner
 * @author Keith Seitz  <keiths@redhat.com>
 */
public class SessionsProcessor extends XMLProcessor {
	public static class SessionInfo {
		// The oprofile info
		public OpInfo info;
		
		// A list of SessionEvents
		public ArrayList list;
	};
	
	// XML tags recognized by this processor
	public static final String SESSION_TAG = "session"; //$NON-NLS-1$
	private static final String _SESSION_NAME_ATTR = "name"; //$NON-NLS-1$
	public static final String SAMPLE_COUNT_TAG = "count"; //$NON-NLS-1$
	public static final String EVENT_TAG = "event"; //$NON-NLS-1$
	private static final String _EVENT_NAME_ATTR = "name"; //$NON-NLS-1$
	
	// The current session being constructed
	private SampleSession _currentSession;
	
	// The current event being constructed
	private SessionEvent _currentEvent;
	
	// A list of all sessions
	private ArrayList _sessionList;
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.oprofile.core.opxml.XMLProcessor#startElement(java.lang.String, org.xml.sax.Attributes, java.lang.Object)
	 */
	public void startElement(String name, Attributes attrs, Object callData) {
		if (name.equals(SESSION_TAG)) {
			SessionInfo info = (SessionInfo) callData;
			String sessionName = attrs.getValue(_SESSION_NAME_ATTR);
			File file = null;
			if (sessionName.length() > 0) {
				file = new File(info.info.getDir() + sessionName);
			}
			_currentSession = new SampleSession(file, _currentEvent.eventName);
		} else if (name.equals(EVENT_TAG)) {
			String eventName = attrs.getValue(_EVENT_NAME_ATTR);
			_currentEvent = new SessionEvent(eventName);
			_sessionList = new ArrayList();
		} else {
			super.startElement(name, attrs, callData);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.oprofile.core.opxml.XMLProcessor#endElement(java.lang.String, java.lang.Object)
	 */
	public void endElement(String name, Object callData) {
		if (name.equals(SAMPLE_COUNT_TAG)) {
			// Got count of samples in session. Save into current session.
			_currentSession.setSampleCount(Integer.parseInt(_characters));
		} else if (name.equals(SESSION_TAG)) {
			// Got end of session -- save in session list
			_sessionList.add(_currentSession);
			_currentSession = null;
		} else if (name.equals(EVENT_TAG)) {
			// Got end of event -- save session list into current SessionEvent and
			// save current event into call data
			_currentEvent.sessions = new SampleSession[_sessionList.size()];
			_sessionList.toArray(_currentEvent.sessions);
			SessionInfo info = (SessionInfo) callData;
			info.list.add(_currentEvent);
			_currentEvent = null;
			_sessionList = null;
		} else {
			super.endElement(name, callData);
		}
	}
}
