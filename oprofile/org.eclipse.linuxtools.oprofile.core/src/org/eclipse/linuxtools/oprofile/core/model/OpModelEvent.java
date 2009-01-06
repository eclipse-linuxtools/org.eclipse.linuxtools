/*******************************************************************************
 * Copyright (c) 2004,2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Keith Seitz <keiths@redhat.com> - initial API and implementation
 *    Kent Sebastian <ksebasti@redhat.com> - 
 *******************************************************************************/ 

package org.eclipse.linuxtools.oprofile.core.model;

/**
 * A class which represents the event collected in a given session.
 */
public class OpModelEvent {
	private String _eventName;
	private OpModelSession[] _sessions;
	private String _printTabs = "";		//for nice output 
	
	public OpModelEvent(String name) {
		_eventName = name;
	}

	public OpModelSession[] getSessions() {
		return _sessions;
	}

	public void setSessions(OpModelSession[] sessions) {
		_sessions = sessions;
	}

	public String getName() {
		return _eventName;
	}

	public void setName(String name) {
		_eventName = name;
	}
	
	//populate all sessions
	public void refreshModel() {
		if (_sessions != null) {
			for (int i = 0; i < _sessions.length; i++) {
				_sessions[i].refreshModel();
			}
		}
	}
	
	public String toString(String tabs) {
		_printTabs = tabs;
		String s = toString();
		_printTabs = "";
		return s;
	}

	@Override
	public String toString() {
		String s = _eventName + "\n";
		if (_sessions != null)
		{
			for (int i = 0; i < _sessions.length; i++) {
				s += _printTabs + "Session: ";
				s += _sessions[i].toString(_printTabs + "\t");
			}
		}
		return s;
	}
}
