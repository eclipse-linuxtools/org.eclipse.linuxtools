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
package org.eclipse.linuxtools.oprofile.ui.model;

import java.util.ArrayList;

import org.eclipse.linuxtools.oprofile.core.model.OpModelEvent;
import org.eclipse.swt.graphics.Image;

public class UiModelEvent implements IUiModelElement {
	private OpModelEvent _event;
	private UiModelSession _sessions[];
	
	public UiModelEvent(OpModelEvent event) {
		_event = event;
		refreshModel();
	}

	/**
	 * Create the ui sessions from the data model.
	 */
	private void refreshModel() {
		ArrayList<UiModelSession> sessionList = new ArrayList<UiModelSession>();

		//TODO the stuff here that parses them
		
		_sessions = new UiModelSession[sessionList.size()];
		sessionList.toArray(_sessions);
	}
	
	@Override
	public IUiModelElement[] getChildren() {
		return _sessions;
	}

	@Override
	public Image getLabelImage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLabelText() {
		return toString();
	}

	/**
	 * Parent is the ui model root, which isn't displayed 
	 * in the tree viewer.
	 */
	@Override
	public IUiModelElement getParent() {
		return null;
	}

	@Override
	public boolean hasChildren() {
		return (_sessions.length == 0 ? false : true);
	}
	
	@Override
	public String toString() {
		return _event.getName();
	}


}
