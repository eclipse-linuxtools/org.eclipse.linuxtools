/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/

package org.eclipse.cdt.oprofile.ui;

import org.eclipse.cdt.oprofile.ui.internal.IProfileElement;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.util.Assert;


/**
 * This class is the content provider for *ProfileViews.
 * @author keiths
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
