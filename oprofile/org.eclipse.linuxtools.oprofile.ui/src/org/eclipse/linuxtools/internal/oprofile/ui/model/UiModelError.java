/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kent Sebastian <ksebasti@redhat.com> - initial API and implementation 
 *******************************************************************************/ 
package org.eclipse.linuxtools.internal.oprofile.ui.model;

import org.eclipse.linuxtools.internal.oprofile.ui.OprofileUiMessages;
import org.eclipse.linuxtools.internal.oprofile.ui.OprofileUiPlugin;
import org.eclipse.swt.graphics.Image;

public class UiModelError implements IUiModelElement {
	public static final UiModelError NO_SAMPLES_ERROR = new UiModelError(OprofileUiMessages.getString("root.error.nosamples")); //$NON-NLS-1$

	private String errorMessage;
	
	public UiModelError(String message) {
		errorMessage = message;
	}
	
	/** IUiModelElement functions **/
	public String getLabelText() {
		return errorMessage;
	}

	public IUiModelElement[] getChildren() {
		return null;
	}
	
	public boolean hasChildren() {
		return false;
	}

	public IUiModelElement getParent() {
		return null;
	}
	
	public Image getLabelImage() {
		return OprofileUiPlugin.getImageDescriptor(OprofileUiPlugin.ERROR_ICON).createImage();
	}
}
