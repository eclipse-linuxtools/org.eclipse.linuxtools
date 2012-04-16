/*******************************************************************************
 * Copyright (c) 2004, 2008, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Keith Seitz <keiths@redhat.com> - initial API and implementation
 *    Kent Sebastian <ksebasti@redhat.com> - 
 *******************************************************************************/ 

package org.eclipse.linuxtools.internal.oprofile.core.model;

/**
 * A class which represents the event collected in a given session.
 */
public class OpModelEvent {
	private String eventName;
	private OpModelSession[] sessions;
	private String printTabs = "";		//for nice output  //$NON-NLS-1$
	
	public OpModelEvent(String name) {
		eventName = name;
	}

	public OpModelSession[] getSessions() {
		return sessions;
	}

	public void setSessions(OpModelSession[] sessions) {
		this.sessions = sessions;
	}

	public String getName() {
		return eventName;
	}

	//populate all sessions
	public void refreshModel() {
		if (sessions != null) {
			for (int i = 0; i < sessions.length; i++) {
				sessions[i].refreshModel();
			}
		}
	}
	
	public String toString(String tabs) {
		printTabs = tabs;
		String s = toString();
		printTabs = ""; //$NON-NLS-1$
		return s;
	}

	@Override
	public String toString() {
		String s = eventName + "\n"; //$NON-NLS-1$
		if (sessions != null) {
			for (int i = 0; i < sessions.length; i++) {
				s += printTabs + "Session: "; //$NON-NLS-1$
				s += sessions[i].toString(printTabs + "\t"); //$NON-NLS-1$
			}
		}
		return s;
	}
}
