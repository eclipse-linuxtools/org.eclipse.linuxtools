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

package org.eclipse.linuxtools.oprofile.ui.system;

import java.util.ArrayList;

import org.eclipse.linuxtools.oprofile.core.model.OpModelSample;
import org.eclipse.linuxtools.oprofile.ui.internal.IProfileElement;
import org.eclipse.linuxtools.oprofile.ui.internal.ProfileElement;
import org.eclipse.swt.graphics.Image;

public class SystemProfileSymbol extends ProfileElement
{
	private int _count = -1;
	private ArrayList _samples = new ArrayList();
	private ArrayList _children = new ArrayList();
		
	public SystemProfileSymbol(IProfileElement parent)
	{
		super(parent, IProfileElement.SYMBOL);
	}
	
	public void addSample(OpModelSample s)
	{
		_samples.add(s);
		_children.add(new SystemProfileSample(this, s));
	}
	
	/**
	 * @see org.eclipse.linuxtools.oprofile.ui.internal.IProfileElement#getChildren()
	 */
	public IProfileElement[] getChildren()
	{
		// Children are simply the samples
		IProfileElement[] children = new IProfileElement[_children.size()];
		_children.toArray(children);
		return children;
	}

	/**
	 * @see org.eclipse.linuxtools.oprofile.ui.internal.IProfileElement#hasChildren()
	 */
	public boolean hasChildren()
	{
		return true;
	}

	/**
	 * @see org.eclipse.linuxtools.oprofile.ui.internal.IProfileElement#getLabelImage()
	 */
	public Image getLabelImage()
	{
		return null;
	}

	/**
	 * @see org.eclipse.linuxtools.oprofile.ui.internal.IProfileElement#getLabelText()
	 */
	public String getLabelText()
	{
		return "";//((OpModelSample)_samples.get(0)).getSymbol().name;
	}

	public int getSampleCount()
	{
		// there appears to be a lack of symmetry... Should be
		// a SampleSymbol object with Samples associated with it?
		if (_count < 0)
		{
			_count = 0;
			for (int i = 0; i < _samples.size(); ++i)
				_count += ((OpModelSample) _samples.get(i)).getCount();
		}
		
		return _count;
	}
	
	public String getAddress()
	{
		return "0xdeadbeef";//((OpModelSample)_samples.get(0)).getSymbol().startAddress;
	}
}
