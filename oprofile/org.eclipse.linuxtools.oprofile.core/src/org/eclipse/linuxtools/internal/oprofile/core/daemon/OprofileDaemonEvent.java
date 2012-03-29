/*******************************************************************************
 * Copyright (c) 2004,2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Keith Seitz <keiths@redhat.com> - initial API and implementation
 *    Kent Sebastian <ksebasti@redhat.com>
 *******************************************************************************/ 

package org.eclipse.linuxtools.internal.oprofile.core.daemon;

import org.eclipse.linuxtools.internal.oprofile.core.Oprofile;

/**
 * This class represents an event used to configure the OProfile
 * daemon.
 */
public class OprofileDaemonEvent {
	public static final int COUNT_UNINITIALIZED = 0;
	public static final int COUNT_INVALID = -1;
	
	// The event to collect on this counter
	private OpEvent _event;
	
	// Profile kernel?
	private boolean _profileKernel;
	
	// Profile userspace?
	private boolean _profileUser;
	
	// Reset counter value
	private int _count;

	public OprofileDaemonEvent() {
		_profileKernel = true;
		_profileUser = true;
		_count = COUNT_UNINITIALIZED;
		_event = null;
	}

	/**
	 * Set the event to collect
	 * @param event the OProfile event
	 */
	public void setEvent(OpEvent event) {
		_event = event;
	}
	
	/**
	 * Get the event to collect
	 * @returns the OProfile event
	 */
	public OpEvent getEvent() {
		return _event;
	}

	/**
	 * Set whether to profile the kernel
	 * @param profileKernel whether to enable kernel profiling
	 */
	public void setProfileKernel(boolean profileKernel) {
		_profileKernel = profileKernel;
	}
	
	/**
	 * Get whether to profile the kernel
	 * @return whether to profile the kernel
	 */
	public boolean getProfileKernel() {
		return _profileKernel;
	}

	/**
	 * Set whether to profile userspace
	 * @param profileUser whether to profile userspace
	 */
	public void setProfileUser(boolean profileUser) {
		_profileUser = profileUser;
	}
	
	/**
	 * Get whether to profile userspace
	 * @return whether to profile userspace
	 */
	public boolean getProfileUser() {
		return _profileUser;
	}

	/**
	 * Set the reset count
	 * @param count the new count
	 */
	public void setResetCount(int count) {
		_count = count;
	}
	
	/**
	 * Get the reset count
	 * @return the reset count
	 */
	public int getResetCount() {
		// FIXME: This isn't quite in the right place...
		if (_count == COUNT_UNINITIALIZED) {
			// This is what Oprofile does in oprof_start.cpp:
			double speed = Oprofile.getCpuFrequency();
			if (speed == 0.0) {
				_count = _event.getMinCount() * 30;
			} else {
				_count = (int) speed * 20;
			}
		}
		
		return _count;
	}
}
