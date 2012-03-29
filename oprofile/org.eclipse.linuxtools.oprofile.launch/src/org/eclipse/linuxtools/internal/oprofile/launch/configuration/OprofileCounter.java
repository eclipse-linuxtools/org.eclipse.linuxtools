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

package org.eclipse.linuxtools.internal.oprofile.launch.configuration;

import java.text.MessageFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.linuxtools.internal.oprofile.core.Oprofile;
import org.eclipse.linuxtools.internal.oprofile.core.daemon.OpEvent;
import org.eclipse.linuxtools.internal.oprofile.core.daemon.OpUnitMask;
import org.eclipse.linuxtools.internal.oprofile.core.daemon.OprofileDaemonEvent;
import org.eclipse.linuxtools.internal.oprofile.launch.OprofileLaunchMessages;
import org.eclipse.linuxtools.internal.oprofile.launch.OprofileLaunchPlugin;

/**
 * This class represents an oprofile runtime configuration of a counter. It is
 * used to construct arguments for launching op_start on the host. It
 * simply wraps OprofileDaemonEvent.
 */
public class OprofileCounter {
	private static final String COUNTER_STRING = OprofileLaunchMessages.getString("oprofileCounter.counterString"); //$NON-NLS-1$
	
	// The counter number
	private int _number;
	
	// Is this counter enabled?
	private boolean _enabled;
	
	// The event to collect on this counter
	private OprofileDaemonEvent _daemonEvent;
	
	// List of valid events on this counter
	private OpEvent[] _eventList = null;

	/**
	 * Constructor for OprofileCounter.
	 * @param nr	the counter number
	 */
	public OprofileCounter(int nr) {
		_number = nr;
		_enabled = false;
		_eventList = Oprofile.getEvents(_number);
		_daemonEvent = new OprofileDaemonEvent();
	}

	/**
	 * Constructs all of the counters in  the given launch configuration.
	 * @param config the launch configuration
	 * @return an array of all counters
	 */
	public static OprofileCounter[] getCounters(ILaunchConfiguration config) {
		OprofileCounter[] ctrs = new OprofileCounter[Oprofile.getNumberOfCounters()];
		for (int i = 0; i < ctrs.length; i++)
		{
			ctrs[i] = new OprofileCounter(i);
			if (config != null)
				ctrs[i].loadConfiguration(config);
		}
		
		return ctrs;
	}

	/**
	 * Method setEnabled.
	 * @param enabled	whether to set this counter as enabled
	 */
	public void setEnabled(boolean enabled) {
		_enabled = enabled;
	}
	
	/**
	 * Method setEvent.
	 * @param event	the event for this counter
	 */
	public void setEvent(OpEvent event) {
		_daemonEvent.setEvent(event);
	}
	
	/**
	 * Method setProfileKernel.
	 * @param profileKernel	whether this counter should count kernel events
	 */
	public void setProfileKernel(boolean profileKernel) {
		_daemonEvent.setProfileKernel(profileKernel);
	}
	
	/**
	 * Method setProfileUser.
	 * @param profileUser	whether this counter should count user events
	 */
	public void setProfileUser(boolean profileUser) {
		_daemonEvent.setProfileUser(profileUser);
	}
	
	/**
	 * Method setCount.
	 * @param count	the number of events between samples for this counter
	 */
	public void setCount(int count) {
		_daemonEvent.setResetCount(count);
	}
	
	/**
	 * Saves this counter's configuration into the specified launch
	 * configuration.
	 * @param config	the launch configuration
	 */
	public void saveConfiguration(ILaunchConfigurationWorkingCopy config) {
		config.setAttribute(OprofileLaunchPlugin.ATTR_COUNTER_ENABLED(_number), _enabled);
		if (_daemonEvent.getEvent() != null) {
			config.setAttribute(OprofileLaunchPlugin.ATTR_COUNTER_EVENT(_number), _daemonEvent.getEvent().getText());
			config.setAttribute(OprofileLaunchPlugin.ATTR_COUNTER_UNIT_MASK(_number), _daemonEvent.getEvent().getUnitMask().getMaskValue());
		}
		config.setAttribute(OprofileLaunchPlugin.ATTR_COUNTER_PROFILE_KERNEL(_number), _daemonEvent.getProfileKernel());
		config.setAttribute(OprofileLaunchPlugin.ATTR_COUNTER_PROFILE_USER(_number), _daemonEvent.getProfileUser());
		config.setAttribute(OprofileLaunchPlugin.ATTR_COUNTER_COUNT(_number), _daemonEvent.getResetCount());
	}
	
	/**
	 * Loads a counter configuration from the specified launch configuration.
	 * @param config	the launch configuration
	 */
	public void loadConfiguration(ILaunchConfiguration config) {
		try {
			_enabled = config.getAttribute(OprofileLaunchPlugin.ATTR_COUNTER_ENABLED(_number), false);

			String str = config.getAttribute(OprofileLaunchPlugin.ATTR_COUNTER_EVENT(_number), ""); //$NON-NLS-1$
			_daemonEvent.setEvent(_eventFromString(str));

			if (_daemonEvent.getEvent() == null) {
				return;
			}
			
			
			int maskValue =  config.getAttribute(OprofileLaunchPlugin.ATTR_COUNTER_UNIT_MASK(_number), OpUnitMask.SET_DEFAULT_MASK);
			_daemonEvent.getEvent().getUnitMask().setMaskValue(maskValue);
			
			_daemonEvent.setProfileKernel(config.getAttribute(OprofileLaunchPlugin.ATTR_COUNTER_PROFILE_KERNEL(_number), false));
			_daemonEvent.setProfileUser(config.getAttribute(OprofileLaunchPlugin.ATTR_COUNTER_PROFILE_USER(_number), false));
			
			_daemonEvent.setResetCount(config.getAttribute(OprofileLaunchPlugin.ATTR_COUNTER_COUNT(_number), OprofileDaemonEvent.COUNT_UNINITIALIZED));
		} catch (CoreException ce) {
			
		}
	}
	
	public OpUnitMask getUnitMask() {
		OpEvent event = _daemonEvent.getEvent();
		
		if (event != null) {
			return event.getUnitMask();
		} else {
			return null;
		}
	}
	
	/**
	 * Returns a textual label for this counter (used by UI)
	 * @return the label to use in widgets referring to this counter
	 */
	public String getText() {
		Object[] args = new Object[] {Integer.valueOf(_number)};
		return MessageFormat.format(COUNTER_STRING, args);
	}
	
	/**
	 * Method getNumber.
	 * @return the counter's number
	 */
	public int getNumber() {
		return _number;
	}
	
	/**
	 * Method getEnabled.
	 * @return whether this counter is enabled
	 */
	public boolean getEnabled() {
		return _enabled;
	}

	/**
	 * Method getEvent.
	 * @return the event for this counter
	 */
	public OpEvent getEvent() {
		return _daemonEvent.getEvent();
	}
	
	/**
	 * Method getProfileKernel.
	 * @return whether this counter is counting kernel events
	 */
	public boolean getProfileKernel() {
		return _daemonEvent.getProfileKernel();
	}
	
	/**
	 * Method getProfileUser.
	 * @return whether this counter is counting user events
	 */
	public boolean getProfileUser() {
		return _daemonEvent.getProfileUser();
	}

	/**
	 * Method getCount.
	 * @return the number of events between samples for this counter
	 */
	public int getCount() {
		return _daemonEvent.getResetCount();
	}
	
	/**
	 * Method getValidEvents.
	 * @return an array of all events that this counter can monitor
	 */
	public OpEvent[] getValidEvents() {		
		return _eventList;
	}
	
	/**
	 * Gets the daemon event configuration for this counter.
	 * <B>Not</B> valid if this counter is not enabled!
	 * @return the OprofileDaemonEvent
	 */
	public OprofileDaemonEvent getDaemonEvent() {
		return _daemonEvent;
	}

	// Returns the event with the same label as the parameter STR
	private OpEvent _eventFromString(String str) {
		for (int i = 0; i < _eventList.length; i++) {
			if (_eventList[i].getText().equals(str))
				return _eventList[i];
		}
		
		return null;
	}
}

