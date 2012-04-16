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
 * This class represents oprofile sessions. Sessions contain an image
 * of the profiled binary.
 */
public class OpModelSession {
	private static final String DEFAULT_SESSION_STRING = "current"; //$NON-NLS-1$

	private OpModelEvent parentEvent;
	private OpModelImage image;
	private String name;
	private String printTabs = "";		//for nice output //$NON-NLS-1$

	public OpModelSession(OpModelEvent event, String name) {
		parentEvent = event;
		this.name = name;
		image = null;
	}
	
	public OpModelImage getImage() {
		return image;
	}

	public OpModelEvent getEvent() {
		return parentEvent;
	}
	
	public String getName() {
		return name;
	}
	
	public int getCount() {
		if (image == null) {
			return 0;
		} else {
			return image.getCount();
		}
	}
	
	public boolean isDefaultSession() {
		return name.equals(DEFAULT_SESSION_STRING); 
	}
	
	public void refreshModel() {
		//populate this session with samples
		image = getNewImage();
	}
	
	protected OpModelImage getNewImage() {
		return Oprofile.getModelData(parentEvent.getName(), name);
	}

	public String toString(String tabs) {
		printTabs = tabs;
		String s = toString();
		printTabs = ""; //$NON-NLS-1$
		return s;
	}

	@Override
	public String toString() {
		String s = name + "\n"; //$NON-NLS-1$
		if (image != null) {
			s += printTabs + "Image: "; //$NON-NLS-1$
			s += image.toString(printTabs + "\t"); //$NON-NLS-1$
		}
		return s;
		
	}
}
