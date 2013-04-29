/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kent Sebastian <ksebasti@redhat.com> - initial API and implementation,
 *    	adapted from Keith Seitz's ProfileContentProvider 
 *******************************************************************************/ 
package org.eclipse.linuxtools.internal.oprofile.ui.view;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.linuxtools.oprofile.ui.model.IUiModelElement;

/**
 * Content provider for the OprofileView's tree viewer.
 */
public class OprofileViewContentProvider implements ITreeContentProvider {

	public Object[] getChildren(Object element) {
		Assert.isLegal(element instanceof IUiModelElement, "in OprofileViewContentProvider"); //$NON-NLS-1$
		return ((IUiModelElement) element).getChildren();
	}

	public Object getParent(Object element) {
		Assert.isLegal(element instanceof IUiModelElement, "in OprofileViewContentProvider"); //$NON-NLS-1$
		return ((IUiModelElement) element).getParent();
	}

	public boolean hasChildren(Object element) {
		Assert.isLegal(element instanceof IUiModelElement, "in OprofileViewContentProvider"); //$NON-NLS-1$
		return ((IUiModelElement) element).hasChildren();
	}

	public Object[] getElements(Object parentElement) {
		return getChildren(parentElement);
	}
	
	public void dispose() {
		// TODO Auto-generated method stub
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// TODO Auto-generated method stub
	}

}
