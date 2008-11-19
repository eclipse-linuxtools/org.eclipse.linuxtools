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

import org.eclipse.linuxtools.oprofile.ui.internal.IProfileElement;
import org.eclipse.linuxtools.oprofile.ui.internal.ProfileElement;
import org.eclipse.swt.graphics.Image;


public class SystemProfileEvent extends ProfileElement
{
	private ArrayList _sessions;
	
	/**
	 * Constructor for SystemProfileEvent.
	 */
	public SystemProfileEvent()
	{
		super(null, IProfileElement.ROOT);
		_sessions = new ArrayList();
	}
	
	public void add(SystemProfileSession session)
	{
		_sessions.add(session);
	}
	
	/**
	 * @see org.eclipse.linuxtools.oprofile.ui.internal.IProfileElement#getSampleCount()
	 */
	public int getSampleCount()
	{
		// FIXME: Meaningless, but I guess we could display something?
		return 0;
	}

	/**
	 * @see org.eclipse.linuxtools.oprofile.ui.internal.IProfileElement#getChildren()
	 */
	public IProfileElement[] getChildren()
	{
		// Children are the sessions
		IProfileElement[] children = new IProfileElement[_sessions.size()];
		if (children.length > 0)
			_sessions.toArray(children);
		return children;
	}

	/**
	 * @see org.eclipse.linuxtools.oprofile.ui.internal.IProfileElement#hasChildren()
	 */
	public boolean hasChildren()
	{
		return (_sessions.size() > 0);
	}

	/**
	 * @see org.eclipse.linuxtools.oprofile.ui.internal.IProfileElement#getLabelImage()
	 */
	public Image getLabelImage()
	{
		// NOT USED
		return null;
	}

	/**
	 * @see org.eclipse.linuxtools.oprofile.ui.internal.IProfileElement#getLabelText()
	 */
	public String getLabelText()
	{
		// NOT USED
		return null;
	}
}
