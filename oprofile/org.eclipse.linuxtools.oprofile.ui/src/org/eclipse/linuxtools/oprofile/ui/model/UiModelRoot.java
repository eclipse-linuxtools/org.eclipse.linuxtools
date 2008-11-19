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
 * Convenience class for parsing the UI data model from the oprofile data model.
 */
public class UiModelRoot implements IUiModelElement {
	//singleton
	private static UiModelRoot _uiModelRoot = new UiModelRoot();

	//event children
	private UiModelEvent[] _events;

	/**
	 * constructor, private for singleton
	 */
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
	
	public void refreshModel() {
		OpModelRoot modelRoot = OpModelRoot.getDefault();
		OpModelEvent dataModelEvents[] = modelRoot.getEvents();

		_events = new UiModelEvent[dataModelEvents.length];
		for (int i = 0; i < dataModelEvents.length; i++) {
			_events[i] = new UiModelEvent(dataModelEvents[i]);
		}
	}
	
	public UiModelEvent[] getEvents() {
		return _events;
	}

	@Override
	public IUiModelElement[] getChildren() {
		return getEvents();
	}

	@Override
	public Image getLabelImage() {
		return null;
	}

	@Override
	public String getLabelText() {
		return null;
	}

	@Override
	public IUiModelElement getParent() {
		return null;
	}

	@Override
	public boolean hasChildren() {
		return (_events.length == 0 ? false : true);
	}

	
}
