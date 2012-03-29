/*******************************************************************************
 * Copyright (c) 2008,2009 Red Hat, Inc.
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
import org.eclipse.linuxtools.internal.oprofile.core.model.OpModelRoot;
import org.eclipse.swt.graphics.Image;

/**
 * Convenience class for creating the UI model from the oprofile data model,
 *  via a single point of access.
 */
public class UiModelRoot implements IUiModelElement {
	private static UiModelRoot _uiModelRoot = new UiModelRoot();	//singleton
	private UiModelEvent[] _events;							//this node's children
	private UiModelError _rootError;

	/** constructor, private for singleton use **/
	protected UiModelRoot() {
//		refreshModel();
		_events = null;
		_rootError = null;
//		_uiModelRoot = this;
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
		OpModelEvent dataModelEvents[] = getModelDataEvents();
		
		_rootError = null;
		_events = null;

		if (dataModelEvents == null || dataModelEvents.length == 0) {
			_rootError = UiModelError.NO_SAMPLES_ERROR;
		} else {
			_events = new UiModelEvent[dataModelEvents.length];
			for (int i = 0; i < dataModelEvents.length; i++) {
				_events[i] = new UiModelEvent(dataModelEvents[i]);
			}
		}
	}
	
	protected OpModelEvent[] getModelDataEvents() {
		OpModelRoot modelRoot = OpModelRoot.getDefault();
		return modelRoot.getEvents();
	}

	/** IUiModelElement functions **/
	public String getLabelText() {
		return null;
	}

	public IUiModelElement[] getChildren() {
		if (_events != null)
			return _events;
		else
			return new IUiModelElement[] { _rootError };
	}

	public boolean hasChildren() {
		return true;
	}

	public IUiModelElement getParent() {
		return null;
	}

	public Image getLabelImage() {
		return null;
	}
}
