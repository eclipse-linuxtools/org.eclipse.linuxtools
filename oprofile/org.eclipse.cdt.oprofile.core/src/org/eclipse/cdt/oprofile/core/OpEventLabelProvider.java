/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/
package org.eclipse.cdt.oprofile.core;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

/**
 * This class is a label provider for OpEvents. Primarily used by the launcher,
 * it is possible it could be used by other packages.
 * @author keiths
 */
public class OpEventLabelProvider implements ILabelProvider
{

	/**
	 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(Object)
	 */
	public Image getImage(Object arg0)
	{
		return null;
	}

	/**
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(Object)
	 */
	public String getText(Object element)
	{
		OpEvent e = (OpEvent) element;
		return e.getText();
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
