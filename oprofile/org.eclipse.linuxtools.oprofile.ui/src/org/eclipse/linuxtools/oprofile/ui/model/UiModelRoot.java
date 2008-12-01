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

import org.eclipse.linuxtools.oprofile.core.model.OpModelEvent;
import org.eclipse.linuxtools.oprofile.core.model.OpModelRoot;
import org.eclipse.swt.graphics.Image;

/**
 * Convenience class for creating the UI model from the oprofile data model,
 *  via a single point of access.
 */
public class UiModelRoot implements IUiModelElement {
	private static UiModelRoot _uiModelRoot = new UiModelRoot();	//singleton
	private UiModelEvent[] _events;									//this node's children


	/** constructor, private for singleton use **/
	private UiModelRoot() {
		refreshModel();
		_uiModelRoot = this;
	}
	
	/**
	 * Get the instance of this ui model root.
	 * @return the ui model root object
	 */
	public static UiModelRoot getDefault() {
		return _uiModelRoot;
	}

	/**
	 * Kick off creating the UI model from the data model. Meant to 
	 * 	be called from UI code. The refreshModel() method is called for 
	 *  the child elements from their constructor.
	 */
	public void refreshModel() {
		OpModelRoot modelRoot = OpModelRoot.getDefault();
		OpModelEvent dataModelEvents[] = modelRoot.getEvents();

		_events = new UiModelEvent[dataModelEvents.length];
		for (int i = 0; i < dataModelEvents.length; i++) {
			_events[i] = new UiModelEvent(dataModelEvents[i]);
		}
	}

	/** IUiModelElement functions **/
	public String getLabelText() {
		return null;
	}

	public IUiModelElement[] getChildren() {
		return _events;
	}

	public boolean hasChildren() {
		return (_events.length == 0 ? false : true);
	}

	public IUiModelElement getParent() {
		return null;
	}

	public Image getLabelImage() {
		return null;
	}
}
