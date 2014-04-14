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

import java.util.Arrays;

import org.eclipse.linuxtools.internal.oprofile.core.model.OpModelRoot;
import org.eclipse.linuxtools.internal.oprofile.core.model.OpModelSession;
import org.eclipse.swt.graphics.Image;

/**
 * Convenience class for creating the UI model from the oprofile data model,
 *  via a single point of access.
 * @since 1.1
 */
public class UiModelRoot implements IUiModelElement {
	private static UiModelRoot uiModelRoot = new UiModelRoot();	//singleton

	private UiModelError rootError;
	private UiModelSession[] session;						//this node's children

	/** constructor, private for singleton use **/
	protected UiModelRoot() {
		session = null;
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
		OpModelSession dataModelEvents[] = getModelDataEvents();


		rootError = null;
		session = null;

		if (dataModelEvents == null || dataModelEvents.length == 0) {
			rootError = UiModelError.NO_SAMPLES_ERROR;
		} else {
			session = new UiModelSession[dataModelEvents.length];
			for (int i = 0; i < dataModelEvents.length; i++) {
				session[i] = new UiModelSession(dataModelEvents[i]);
			}
		}
	}

	protected OpModelSession[] getModelDataEvents() {
		OpModelRoot modelRoot = OpModelRoot.getDefault();
		return modelRoot.getSessions();
	}

	/** IUiModelElement functions **/
	@Override
	public String getLabelText() {
		return null;
	}

	/**
	 * Returns the children of this element.
	 * @return An array of child elements or null
	 */
	@Override
	public IUiModelElement[] getChildren() {
		if (session != null && session.length != 0) {
			if (UiModelRoot.SortType.SESSION == UiModelRoot.getSortingType()) {
				Arrays.sort(session, UiModelSorting.getInstance());
				return session;
			}

			else {
				return session;
			}

		} else
			return new IUiModelElement[] { rootError };
	}
	/**
	 * Returns if the element has any children.
	 * @return true if the element has children, false otherwise
	 */
	@Override
	public boolean hasChildren() {
		return true;
	}

	/**
	 * Returns the element's parent.
	 * @return The parent element or null
	 */
	@Override
	public IUiModelElement getParent() {
		return null;
	}

	@Override
	public Image getLabelImage() {
		return null;
	}

	/**
	 *
	 * Adding sorting feature in tree.
	 * @since 3.0
	 *
	 */
	public static enum SortType{DEFAULT,SESSION,EVENT,LIB,FUNCTION,LINE_NO}
	private static SortType sortType;
	public static void setSortingType(SortType sortType) {
		UiModelRoot.sortType = sortType;
	}

	public static SortType getSortingType() {
		return UiModelRoot.sortType;
	}
}
