/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kent Sebastian <ksebasti@redhat.com> - initial API and implementation 
 *******************************************************************************/ 

package org.eclipse.linuxtools.oprofile.core.model;

import org.eclipse.linuxtools.oprofile.core.Oprofile;

/**
 * A root node for the data model. Only one instance exists at any time,
 * although the contents will change. On instantiation the events and
 * sessions are gathered. 
 * 
 * Note that this data model does not map 1:1 to the oprofile data model.
 * This model is for use in profiling one application compiled with debug
 * info, from within eclipse. 
 */

public class OpModelRoot {
	//single instance
	private static OpModelRoot _modelRoot = new OpModelRoot();

	private OpModelEvent[] _events;
//	private String _printTabs = "\t";		//for nice output

	private OpModelRoot() {
		refreshModel();
		_modelRoot = this;
	}

	public static OpModelRoot getDefault() {
		return _modelRoot;
	}

	public void refreshModel() {
		//TODO-performance/interactivity: some persistence for events/sessions
		// that dont change from run to run (non default sessions) 
		
		//launch `opxml sessions`, gather up events & the sessions under them
		_events = Oprofile.getEvents();
		if (_events != null) {
			for (int i = 0; i < _events.length; i++) {
				_events[i].refreshModel();
			}
		}
	}
	
	public OpModelEvent[] getEvents() {
		return _events;
	}
	
	@Override
	public String toString() {
		String s = "";
		for (int i = 0; i < _events.length; i++) {
			s += "Event: ";
			s += _events[i].toString("\t");
		}
		return s;
	}
}
