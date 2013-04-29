/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kent Sebastian <ksebasti@redhat.com> - initial API and implementation,
 *    	adapted from Keith Seitz's ProfileLabelProvider 
 *******************************************************************************/ 
package org.eclipse.linuxtools.internal.oprofile.ui.view;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.linuxtools.oprofile.ui.model.IUiModelElement;
import org.eclipse.swt.graphics.Image;

/**
 * Content provider for OprofileView's tree viewer.
 */
public class OprofileViewLabelProvider implements ILabelProvider {

	public Image getImage(Object element) {
		Assert.isLegal(element instanceof IUiModelElement, "in OprofileViewLabelProvider"); //$NON-NLS-1$
		return ((IUiModelElement) element).getLabelImage();
	}

	public String getText(Object element) {
		Assert.isLegal(element instanceof IUiModelElement, "in OprofileViewLabelProvider"); //$NON-NLS-1$
		return ((IUiModelElement) element).getLabelText();
	}

	public void addListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub
	}

	public void dispose() {
		// TODO Auto-generated method stub
	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub
	}

}
