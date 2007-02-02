/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/

package org.eclipse.cdt.oprofile.ui;

import org.eclipse.cdt.oprofile.ui.internal.IProfileElement;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.util.Assert;


/**
 * This class is the content provider for the *ProfileViews.
 * @author keiths
 */
public class ProfileLabelProvider implements ILabelProvider
{
	/**
	 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(Object)
	 */
	public Image getImage(Object element)
	{
		Assert.isLegal(element instanceof IProfileElement, "in ProfileLabelProvider"); //$NON-NLS-1$
		return ((IProfileElement) element).getLabelImage();
	}

	/**
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(Object)
	 */
	public String getText(Object element)
	{
		Assert.isLegal(element instanceof IProfileElement, "in ProfileLabelProvider"); //$NON-NLS-1$
		return ((IProfileElement) element).getLabelText();
	}

	/**
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(ILabelProviderListener)
	 */
	public void addListener(ILabelProviderListener arg0)
	{
	}

	/**
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	public void dispose()
	{
	}

	/**
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(Object, String)
	 */
	public boolean isLabelProperty(Object arg0, String arg1)
	{
		return false;
	}

	/**
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(ILabelProviderListener)
	 */
	public void removeListener(ILabelProviderListener arg0)
	{
	}
}
