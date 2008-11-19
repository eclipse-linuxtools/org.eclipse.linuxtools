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

import org.eclipse.linuxtools.oprofile.core.model.OpModelSample;
import org.eclipse.linuxtools.oprofile.ui.internal.IProfileElement;
import org.eclipse.linuxtools.oprofile.ui.internal.ProfileElement;
import org.eclipse.swt.graphics.Image;

public class SystemProfileSample extends ProfileElement
{
	private OpModelSample _sample;
	
	public SystemProfileSample(IProfileElement parent, OpModelSample s)
	{
		super(parent, IProfileElement.SAMPLE);
		_sample = s;
	}
	
	/**
	 * @see org.eclipse.linuxtools.oprofile.ui.internal.IProfileElement#getChildren()
	 */
	public IProfileElement[] getChildren()
	{
		return null;
	}

	/**
	 * @see org.eclipse.linuxtools.oprofile.ui.internal.IProfileElement#hasChildren()
	 */
	public boolean hasChildren()
	{
		return false;
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
		// This is a sample, so there is no symbol info. Return the VMA from BFD.
		return Integer.toString(_sample.getLine());
	}

	public OpModelSample getSample()
	{
		return _sample;
	}
	
	public int getSampleCount()
	{
		return _sample.getCount();
	}
	
	public int getLineNumber()
	{
		return _sample.getLine();
	}
}
