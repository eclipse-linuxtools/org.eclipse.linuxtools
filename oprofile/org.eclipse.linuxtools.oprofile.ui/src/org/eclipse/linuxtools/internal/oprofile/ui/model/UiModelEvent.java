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
package org.eclipse.linuxtools.internal.oprofile.ui.model;

import org.eclipse.linuxtools.internal.oprofile.core.model.OpModelEvent;
import org.eclipse.linuxtools.internal.oprofile.core.model.OpModelSession;
import org.eclipse.linuxtools.internal.oprofile.ui.OprofileUiPlugin;
import org.eclipse.swt.graphics.Image;

/**
 * Top level elements displayed in the view -- events that oprofile 
 *  has profiled. Must have children sessions.
 */
public class UiModelEvent implements IUiModelElement {
	private IUiModelElement _parent = null;		//parent node -- necessary?
	private OpModelEvent _event;				//the node in the data model
	private UiModelSession _sessions[];			//this node's children
	
	public UiModelEvent(OpModelEvent event) {
		if (event != null) {
			_event = event;
			refreshModel();
		}
	}

	/**
	 * Create the ui sessions from the data model.
	 */
	private void refreshModel() {
		OpModelSession dataModelSessions[] = _event.getSessions();
		_sessions = new UiModelSession[dataModelSessions.length];
		
		for (int i = 0; i < dataModelSessions.length; i++) {
			_sessions[i] = new UiModelSession(this, dataModelSessions[i]);
		}
	}

	@Override
	public String toString() {
		return (_event == null ? "" : _event.getName()); //$NON-NLS-1$
	}

	/** IUiModelElement functions **/
	public String getLabelText() {
		return toString();
	}
	
	public IUiModelElement[] getChildren() {
		return _sessions;
	}

	public boolean hasChildren() {
		return (_sessions == null || _sessions.length == 0 ? false : true);
	}

	public IUiModelElement getParent() {
		return _parent;
	}

	public Image getLabelImage() {
		return OprofileUiPlugin.getImageDescriptor(OprofileUiPlugin.EVENT_ICON).createImage();
	}
}
