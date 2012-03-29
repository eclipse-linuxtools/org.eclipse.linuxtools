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

	private OpModelEvent _parentEvent;
	private OpModelImage _image;
	private String _name;
	private String _printTabs = "";		//for nice output //$NON-NLS-1$

	public OpModelSession(OpModelEvent event, String name) {
		_parentEvent = event;
		_name = name;
		_image = null;
	}
	
	public OpModelImage getImage() {
		return _image;
	}

	public OpModelEvent getEvent() {
		return _parentEvent;
	}
	
	public String getName() {
		return _name;
	}
	
	public int getCount() {
		if (_image == null) {
			return 0;
		} else {
			return _image.getCount();
		}
	}
	
	public boolean isDefaultSession() {
		return _name.equals(DEFAULT_SESSION_STRING); 
	}
	
	public void refreshModel() {
		//populate this session with samples
		_image = getNewImage();
	}
	
	protected OpModelImage getNewImage() {
		return Oprofile.getModelData(_parentEvent.getName(), _name);
	}

	public String toString(String tabs) {
		_printTabs = tabs;
		String s = toString();
		_printTabs = ""; //$NON-NLS-1$
		return s;
	}

	@Override
	public String toString() {
		String s = _name + "\n"; //$NON-NLS-1$
		if (_image != null) {
			s += _printTabs + "Image: "; //$NON-NLS-1$
			s += _image.toString(_printTabs + "\t"); //$NON-NLS-1$
		}
		return s;
		
	}
}
