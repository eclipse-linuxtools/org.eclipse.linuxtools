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

package org.eclipse.linuxtools.oprofile.ui.system;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.eclipse.linuxtools.oprofile.core.model.IOpModelContainer;
import org.eclipse.linuxtools.oprofile.core.model.OpModelSession;
import org.eclipse.linuxtools.oprofile.ui.OprofileUiPlugin;
import org.eclipse.linuxtools.oprofile.ui.OprofileUIMessages;
import org.eclipse.linuxtools.oprofile.ui.internal.IProfileElement;
import org.eclipse.linuxtools.oprofile.ui.internal.ProfileElement;
import org.eclipse.swt.graphics.Image;

/**
 * All the real top elements in the SystemProfileView are Sessions. 
 * There are two types of sessions: real and fake. Real one correspond
 * to real oprofile sessions (i.e., directories in /var/lib/oprofile/samples).
 * The "fake" one is a contrived session which represents a "default" (i.e.,
 * all the files in /var/lib/oprofile/samples).
 */
public class SystemProfileSession extends ProfileElement
{
	protected IProfileElement[] _files = null;
	OpModelSession _session;

	private String TAG_DEFAULT_SESSION = "__default"; // $NON-NLS-1$
	
	SystemProfileSession(OpModelSession session)
	{
		super(null, IProfileElement.SESSION);
		_session = session;
	}
	
	/**
	 * @see org.eclipse.linuxtools.oprofile.ui.internal.IProfileElement#getChildren()
	 */
	public IProfileElement[] getChildren()
	{
		if (_files == null) {
			_files = _getProfileElements();
		}
		return _files;
	}

	/**
	 * @see org.eclipse.linuxtools.oprofile.ui.internal.IProfileElement#hasChildren()
	 */
	public boolean hasChildren()
	{
		// This is a shortcut for hasChildren. We just check if we have
		// samples, since we'll know this before ever loading sample info.
		return (getSampleCount() > 0);
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
		String label = null;
		if (_session.isDefaultSession())
			label = OprofileUIMessages.getString("systemProfileRoot.defaultSession.text"); // $NON-NLS-1$
		else
			label = _session.getName();

		return label;
	}
	
	// Helper function. Returns a list of all the sample files for this sessions (as
	// ProfileElements: SystemProfile{ShLib,Executable,Object}).
	private IProfileElement[] _getProfileElements()
	{
//		ArrayList list = new ArrayList();
//		IOpModelContainer[] sampleFiles = (IOpModelContainer[]) _session.getSampleContainers(OprofileUiPlugin.getActiveWorkbenchShell());
//		for (int i = 0; i < sampleFiles.length; ++i)
//		{
//			// FIXME: This is lame!
//			String exeName = sampleFiles[i].getExecutableName();
//			if (exeName.endsWith(".o")) // $NON-NLS-1$
//				list.add(new SystemProfileObject(this, sampleFiles[i]));
//			else if (exeName.endsWith(".so") || exeName.indexOf(".so") != -1) // $NON-NLS-1$  // $NON-NLS-2$
//				list.add(new SystemProfileShLib(this, sampleFiles[i]));
//			else
//				list.add(new SystemProfileExecutable(this, sampleFiles[i]));
//		}
//		
//		//sort the elements by name
//		Collections.sort(list, new Comparator<IProfileElement>() {
//			@Override
//			public int compare(IProfileElement o1, IProfileElement o2) {
//				return o1.getFileName().compareTo(o2.getFileName());
//			}
//		} );
//	
//		IProfileElement[] elements = new IProfileElement[list.size()];
//		list.toArray(elements);
//		
//		return elements;
		return null;
	}
	
	/**
	 * @see org.eclipse.linuxtools.oprofile.ui.internal.IProfileElement#getSampleCount()
	 */
	public int getSampleCount()
	{
		return _session.getCount();
	}
	
	// This overrides ProfileElement._myId
	protected String _myId()
	{
		String id;
		if (_session.isDefaultSession())
			id = TAG_DEFAULT_SESSION;
		else
			id = _session.getName();
		
		return id;
	}
}
