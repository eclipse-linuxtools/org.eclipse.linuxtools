/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/

package org.eclipse.cdt.oprofile.ui.system;

import java.util.ArrayList;

import org.eclipse.cdt.oprofile.core.Sample;
import org.eclipse.cdt.oprofile.ui.internal.IProfileElement;
import org.eclipse.cdt.oprofile.ui.internal.ProfileElement;
import org.eclipse.swt.graphics.Image;

/**
 * @author keiths
 */
public class SystemProfileSymbol extends ProfileElement
{
	private int _count = -1;
	private ArrayList _samples = new ArrayList();
	private ArrayList _children = new ArrayList();
		
	public SystemProfileSymbol(IProfileElement parent)
	{
		super(parent, IProfileElement.SYMBOL);
	}
	
	public void addSample(Sample s)
	{
		_samples.add(s);
		_children.add(new SystemProfileSample(this, s));
	}
	
	/**
	 * @see org.eclipse.cdt.oprofile.ui.internal.IProfileElement#getChildren()
	 */
	public IProfileElement[] getChildren()
	{
		// Children are simply the samples
		IProfileElement[] children = new IProfileElement[_children.size()];
		_children.toArray(children);
		return children;
	}

	/**
	 * @see org.eclipse.cdt.oprofile.ui.internal.IProfileElement#hasChildren()
	 */
	public boolean hasChildren()
	{
		return true;
	}

	/**
	 * @see org.eclipse.cdt.oprofile.ui.internal.IProfileElement#getLabelImage()
	 */
	public Image getLabelImage()
	{
		return null;
	}

	/**
	 * @see org.eclipse.cdt.oprofile.ui.internal.IProfileElement#getLabelText()
	 */
	public String getLabelText()
	{
		return ((Sample)_samples.get(0)).getSymbol().name;
	}

	public int getSampleCount()
	{
		// there appears to be a lack of symmetry... Should be
		// a SampleSymbol object with Samples associated with it?
		if (_count < 0)
		{
			_count = 0;
			for (int i = 0; i < _samples.size(); ++i)
				_count += ((Sample) _samples.get(i)).getSampleCount();
		}
		
		return _count;
	}
	
	public String getAddress()
	{
		return ((Sample)_samples.get(0)).getSymbol().startAddress;
	}
}
