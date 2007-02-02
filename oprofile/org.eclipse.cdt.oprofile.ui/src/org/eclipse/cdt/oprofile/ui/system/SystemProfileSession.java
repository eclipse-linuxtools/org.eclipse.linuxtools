/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/

package org.eclipse.cdt.oprofile.ui.system;

import java.util.ArrayList;

import org.eclipse.cdt.oprofile.core.ISampleContainer;
import org.eclipse.cdt.oprofile.core.SampleSession;
import org.eclipse.cdt.oprofile.ui.OprofilePlugin;
import org.eclipse.cdt.oprofile.ui.OprofileUIMessages;
import org.eclipse.cdt.oprofile.ui.internal.IProfileElement;
import org.eclipse.cdt.oprofile.ui.internal.ProfileElement;
import org.eclipse.swt.graphics.Image;

/**
 * All the real top elements in the SystemProfileView are Sessions. 
 * There are two types of sessions: real and fake. Real one correspond
 * to real oprofile sessions (i.e., directories in /var/lib/oprofile/samples).
 * The "fake" one is a contrived session which represents a "default" (i.e.,
 * all the files in /var/lib/oprofile/samples).
 * @author keiths
 */
public class SystemProfileSession extends ProfileElement
{
	protected IProfileElement[] _files = null;
	SampleSession _session;

	private String TAG_DEFAULT_SESSION = "__default"; // $NON-NLS-1$
	
	SystemProfileSession(SampleSession session)
	{
		super(null, IProfileElement.SESSION);
		_session = session;
	}
	
	/**
	 * @see org.eclipse.cdt.oprofile.ui.internal.IProfileElement#getChildren()
	 */
	public IProfileElement[] getChildren()
	{
		if (_files == null) {
			_files = _getProfileElements();
		}
		return _files;
	}

	/**
	 * @see org.eclipse.cdt.oprofile.ui.internal.IProfileElement#hasChildren()
	 */
	public boolean hasChildren()
	{
		// This is a shortcut for hasChildren. We just check if we have
		// samples, since we'll know this before ever loading sample info.
		return (getSampleCount() > 0);
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
		String label = null;
		if (_session.isDefaultSession())
			label = OprofileUIMessages.getString("systemProfileRoot.defaultSession.text"); // $NON-NLS-1$
		else
			label = _session.getExecutableName();

		return label;
	}
	
	// Helper function. Returns a list of all the sample files for this sessions (as
	// ProfileElements: SystemProfile{ShLib,Executable,Object}).
	private IProfileElement[] _getProfileElements()
	{
		ArrayList list = new ArrayList();
		ISampleContainer[] sampleFiles = (ISampleContainer[]) _session.getSampleContainers(OprofilePlugin.getActiveWorkbenchShell());
		for (int i = 0; i < sampleFiles.length; ++i)
		{
			// FIXME: This is lame!
			String exeName = sampleFiles[i].getExecutableName();
			if (exeName.endsWith(".o")) // $NON-NLS-1$
				list.add(new SystemProfileObject(this, sampleFiles[i]));
			else if (exeName.endsWith(".so") || exeName.indexOf(".so") != -1) // $NON-NLS-1$  // $NON-NLS-2$
				list.add(new SystemProfileShLib(this, sampleFiles[i]));
			else
				list.add(new SystemProfileExecutable(this, sampleFiles[i]));
		}
	
		IProfileElement[] elements = new IProfileElement[list.size()];
		list.toArray(elements);
		return elements;
	}
	
	/**
	 * @see org.eclipse.cdt.oprofile.ui.internal.IProfileElement#getSampleCount()
	 */
	public int getSampleCount()
	{
		return _session.getSampleCount();
	}
	
	// This overrides ProfileElement._myId
	protected String _myId()
	{
		String id;
		if (_session.isDefaultSession())
			id = TAG_DEFAULT_SESSION;
		else
			id = _session.getExecutableName();
		
		return id;
	}
}
