/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/

package org.eclipse.cdt.oprofile.ui.sample;

import java.util.ArrayList;

import org.eclipse.cdt.oprofile.ui.ProfileContentProvider;
import org.eclipse.cdt.oprofile.ui.internal.IProfileElement;
import org.eclipse.cdt.oprofile.ui.system.SystemProfileSymbol;


/**
 * @author keiths
 */
public class SampleViewContentProvider extends ProfileContentProvider
{
	/**
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(Object)
	 */
	public Object[] getChildren(Object element)
	{
		IProfileElement obj = (IProfileElement) element;
		if (obj.getType () == IProfileElement.SYMBOL)
		{
			// FIXME: should not refer to SystemProfileSymbol
			// *SampleSymbol element's children are *Sample.
			SystemProfileSymbol sym = (SystemProfileSymbol) element;
			return sym.getChildren();
		}
		
		return null;
	}

	/**
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(Object)
	 */
	public boolean hasChildren(Object element)
	{
		// FIXME: Am I really doing this?
		/*		
		// Only types with children are *Symbols. The children are Samples
		IProfileElement obj = (IProfileElement) element;
		if (obj.getType() == IProfileElement.SYMBOL)
			return true;
		*/
		return false;
	}

	/**
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(Object)
	 */
	public Object[] getElements(Object parentElement)
	{
		// returns object to display given the input
		IProfileElement obj = (IProfileElement) parentElement;
		Object[] elements = null;
		
		if (obj.getType() == IProfileElement.SAMPLE) {
			// Samples are a little different. When a request to view a sample occurs,
			// simply display the sample itself.
			elements = new Object[1];
			elements[0] = obj;
		} else {
			// For all other element types, we view the type's children.
			IProfileElement[] children = obj.getChildren();
			
			// One exception: we don't display sessions.
			ArrayList list = new ArrayList();
			for (int i = 0; i < children.length; ++i)
			{
				// FIXME: there must be a better way: add getSampleElements method to IProfileElement?
				if (children[i].getType() != IProfileElement.SESSION)
					list.add(children[i]);
			}
			elements = new Object[list.size()];
			list.toArray(elements);
		}
		
		return elements;
	}
}
