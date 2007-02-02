/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/

package org.eclipse.cdt.oprofile.core;

import java.io.File;

import org.eclipse.swt.widgets.Shell;

/**
 * This class represents oprofile sessions. Sessions are SampleContainers
 * which contain other SampleContainers (SampleFiles). We can have multiple
 * SampleSessions per real oprofile session (one for each counter in a real oprofile
 * session).
 * @author Keith Seitz  <keiths@redhat.com>
 */
public class SampleSession extends SampleContainer
{
	// The count of all samples in this session
	protected int _count;
	
	// The event collected in this session
	protected OpEvent _event;

	/**
	 * Constructor SampleSession.
	 * @param file a File object of this session
	 */
	public SampleSession(File file, String event) {
		super(file);
		_count = 0;
		_event = Oprofile.findEvent(event);
	}
	
	/**
	 * @see org.eclipse.cdt.oprofile.core.ISampleContainer#getSampleContainters()
	 */
	public ISampleContainer[] getSampleContainers(Shell shell) {
		if (_containers.size() == 0) {
			// Haven't read samples for this session yet. Do it now.
			Oprofile.getSamples(this, shell);
		}
		return super.getSampleContainers(shell);
	}
	
	/**
	 * @see org.eclipse.cdt.oprofile.core.ISampleContainer#getSampleCount()
	 */
	public int getSampleCount() {
		return _count;
	}
	
	/**
	 * Sets the sample count for this session
	 * @param count the count
	 */
	public void setSampleCount(int count) {
		_count = count;
	}
	
    /**
	 * Get a description of the event collected in this session.
	 * 
	 * @return the event
	 */
	public OpEvent getEvent() {
		return _event;
	}
		
	/**
	 * Is this session the "default" session?
	 * 
	 * @return whether this session is the default session
	 */
	public boolean isDefaultSession() {
		return Oprofile.isDefaultSession(getFile());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.oprofile.core.ISampleContainer#getExecutableName()
	 */
	public String getExecutableName() {
		return _file.getName();
	}
}
