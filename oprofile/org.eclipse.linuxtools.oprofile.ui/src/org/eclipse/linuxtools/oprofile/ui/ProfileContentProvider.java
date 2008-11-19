/*******************************************************************************
 * Copyright (c) 2004 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Keith Seitz <keiths@redhat.com> - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.linuxtools.oprofile.ui;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.linuxtools.oprofile.ui.internal.IProfileElement;


/**
 * This class is the content provider for *ProfileViews.
 */
public class ProfileContentProvider implements ITreeContentProvider
{
	/**
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(Object)
	 */
	public Object[] getChildren(Object element)
	{
		Assert.isLegal(element instanceof IProfileElement, "in ProfileContentProvider"); //$NON-NLS-1$
		return ((IProfileElement) element).getChildren();
	}

	/**
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(Object)
	 */
	public Object getParent(Object element)
	{
		Assert.isLegal(element instanceof IProfileElement, "in ProfileContentProvider"); //$NON-NLS-1$
		return ((IProfileElement) element).getParent();
	}

	/**
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(Object)
	 */
	public boolean hasChildren(Object element)
	{
		Assert.isLegal(element instanceof IProfileElement, "in ProfileContentProvider"); //$NON-NLS-1$
		return ((IProfileElement) element).hasChildren();
	}

	/**
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(Object)
	 */
	public Object[] getElements(Object parentElement)
	{
		return getChildren(parentElement);
	}

	/**
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose()
	{
	}

	/**
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(Viewer, Object, Object)
	 */
	public void inputChanged(Viewer arg0, Object arg1, Object arg2)
	{
	}
}
