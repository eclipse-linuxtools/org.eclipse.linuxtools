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
package org.eclipse.linuxtools.oprofile.ui.model;

import org.eclipse.linuxtools.internal.oprofile.core.model.OpModelEvent;
import org.eclipse.linuxtools.internal.oprofile.core.model.OpModelRoot;
import org.eclipse.swt.graphics.Image;

/**
 * Convenience class for creating the UI model from the oprofile data model,
 *  via a single point of access.
 */
public class UiModelRoot implements IUiModelElement {
	private static UiModelRoot uiModelRoot = new UiModelRoot();	//singleton
	private UiModelEvent[] events;							//this node's children
	private UiModelError rootError;

	/** constructor, private for singleton use **/
	protected UiModelRoot() {
		events = null;
		rootError = null;
	}
	
	/**
	 * Get the instance of this ui model root.
	 * @return the ui model root object
	 */
	public static UiModelRoot getDefault() {
		return uiModelRoot;
	}

	/**
	 * Kick off creating the UI model from the data model. Meant to 
	 * 	be called from UI code. The refreshModel() method is called for 
	 *  the child elements from their constructor.
	 */
	public void refreshModel() {
		OpModelEvent dataModelEvents[] = getModelDataEvents();
		
		rootError = null;
		events = null;

		if (dataModelEvents == null || dataModelEvents.length == 0) {
			rootError = UiModelError.NO_SAMPLES_ERROR;
		} else {
			events = new UiModelEvent[dataModelEvents.length];
			for (int i = 0; i < dataModelEvents.length; i++) {
				events[i] = new UiModelEvent(dataModelEvents[i]);
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

	/**
	 * Returns the children of this element.
	 * @return An array of child elements or null
	 */
	public IUiModelElement[] getChildren() {
		if (events != null)
			return events;
		else
			return new IUiModelElement[] { rootError };
	}
	/**
	 * Returns if the element has any children.
	 * @return true if the element has children, false otherwise
	 */
	public boolean hasChildren() {
		return true;
	}

	/**
	 * Returns the element's parent.
	 * @return The parent element or null
	 */
	public IUiModelElement getParent() {
		return null;
	}

	public Image getLabelImage() {
		return null;
	}
}
