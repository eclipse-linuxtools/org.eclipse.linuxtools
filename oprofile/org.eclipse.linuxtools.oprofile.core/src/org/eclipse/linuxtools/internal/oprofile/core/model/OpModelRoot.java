/*******************************************************************************
 * Copyright (c) 2008, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kent Sebastian <ksebasti@redhat.com> - initial API and implementation 
 *******************************************************************************/ 

package org.eclipse.linuxtools.internal.oprofile.core.model;

import org.eclipse.linuxtools.internal.oprofile.core.Oprofile;

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
	private static OpModelRoot modelRoot = new OpModelRoot();

	private OpModelEvent[] events;

	protected OpModelRoot() {
		events = null;
	}

	public static OpModelRoot getDefault() {
		return modelRoot;
	}

	public void refreshModel() {
		//TODO-performance/interactivity: some persistence for events/sessions
		// that dont change from run to run (non default sessions) 
		
		events = getNewEvents();
		if (events != null) {
			for (int i = 0; i < events.length; i++) {
				if (events[i] != null)
					events[i].refreshModel();
			}
		}
	}
	
	protected OpModelEvent[] getNewEvents() {
		//launch `opxml sessions`, gather up events & the sessions under them
		return Oprofile.getEvents(); 
	}
	
	public OpModelEvent[] getEvents() {
		return events;
	}
	
	@Override
	public String toString() {
		String s = ""; //$NON-NLS-1$
		if (events != null) {
			for (int i = 0; i < events.length; i++) {
				if (events[i] != null) {
					s += "Event: "; //$NON-NLS-1$
					s += events[i].toString("\t"); //$NON-NLS-1$
				}
			}
		}
		return s;
	}
}
