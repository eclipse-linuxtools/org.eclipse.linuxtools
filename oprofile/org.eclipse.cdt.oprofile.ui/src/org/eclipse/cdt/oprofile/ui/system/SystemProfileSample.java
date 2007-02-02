/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/

package org.eclipse.cdt.oprofile.ui.system;

import org.eclipse.cdt.oprofile.core.Sample;
import org.eclipse.cdt.oprofile.ui.internal.IProfileElement;
import org.eclipse.cdt.oprofile.ui.internal.ProfileElement;
import org.eclipse.swt.graphics.Image;

/**
 * @author keiths
 */
public class SystemProfileSample extends ProfileElement
{
	private Sample _sample;
	
	public SystemProfileSample(IProfileElement parent, Sample s)
	{
		super(parent, IProfileElement.SAMPLE);
		_sample = s;
	}
	
	/**
	 * @see org.eclipse.cdt.oprofile.ui.internal.IProfileElement#getChildren()
	 */
	public IProfileElement[] getChildren()
	{
		return null;
	}

	/**
	 * @see org.eclipse.cdt.oprofile.ui.internal.IProfileElement#hasChildren()
	 */
	public boolean hasChildren()
	{
		return false;
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
		// This is a sample, so there is no symbol info. Return the VMA from BFD.
		return _sample.getAddress();
	}

	public Sample getSample()
	{
		return _sample;
	}
	
	public int getSampleCount()
	{
		return _sample.getSampleCount();
	}
	
	public String getAddress()
	{
		return _sample.getAddress();
	}
	
	public String getFileName()
	{
		return _sample.getFilename();
	}
	
	public int getLineNumber()
	{
		return _sample.getLineNumber();
	}
}
