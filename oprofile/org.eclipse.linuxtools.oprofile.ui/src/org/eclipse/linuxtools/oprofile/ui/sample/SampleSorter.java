/*******************************************************************************
 * Copyright (c) 2004,2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Keith Seitz <keiths@redhat.com> - initial API and implementation
 *    Kent Sebastian <ksebasti@redhat.com> - 
 *******************************************************************************/ 

package org.eclipse.linuxtools.oprofile.ui.sample;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.linuxtools.oprofile.ui.internal.IProfileElement;


public class SampleSorter extends ViewerSorter
{
	private boolean _reversed = false;
	private int _column;
	
	public SampleSorter(int columnNumber)
	{
		_column = columnNumber;
	}
	
	/**
	 * @see org.eclipse.jface.viewers.ViewerSorter#compare(Viewer, Object, Object)
	 */
	public int compare(Viewer viewer, Object obj1, Object obj2)
	{
		IProfileElement o1 = (IProfileElement) obj1;
		IProfileElement o2 = (IProfileElement) obj2;
		
		int result;
		switch (_column)
		{
			case SampleView.COLUMN_LINE:
				result = (o1.getLineNumber() - o2.getLineNumber());
				break;

			case SampleView.COLUMN_NAME:
			 	result = getComparator().compare(o1.getLabelText(), o2.getLabelText());
				break;
			
			case SampleView.COLUMN_PERCENT:
			case SampleView.COLUMN_SAMPLES:
				result = o1.getSampleCount() - o2.getSampleCount();
				break;
			
			case SampleView.COLUMN_VMA:
				// Some elements don't have addresses (or at least we don't know them)
				// In these cases, getAddress will return null. Just pretend they're 0x00.
				String addr1 = (o1.getAddress() == null) ? "0" : o1.getAddress();
				String addr2 = (o2.getAddress() == null) ? "0" : o2.getAddress();
				result = getComparator().compare(addr1, addr2);
				break;
			
			default:
				result = 0;
		}
		
		if (_reversed)
			result = -result;
		
		return result;
	}
	
	public int getColumnNumber()
	{
		return _column;
	}
	
	public boolean isReversed()
	{
		return _reversed;
	}
	
	public void setReversed(boolean reverse)
	{
		_reversed = reverse;
	}
}
