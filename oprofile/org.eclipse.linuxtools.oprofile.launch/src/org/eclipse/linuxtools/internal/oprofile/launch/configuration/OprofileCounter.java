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
import java.util.ArrayList;
import java.util.List;

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
	private int number;
	
	// Is this counter enabled?
	private boolean _enabled;
	
	// The event(s) to collect on this counter
	private OprofileDaemonEvent[] daemonEvent;
	
	// List of valid events on this counter
	private OpEvent[] eventList = null;

	/**
	 * Constructor for OprofileCounter.
	 * @param nr	the counter number
	 */
	public OprofileCounter(int nr) {
		this(nr, Oprofile.getEvents(nr));
	}

	/**
	 * Constructor for OprofileCounter.
	 * @param nr the counter number
	 * @param events the given events for counter number <code>nr</code>
	 */
	public OprofileCounter(int nr, OpEvent[] events) {
		number = nr;
		_enabled = false;
		eventList = events;
		daemonEvent = new OprofileDaemonEvent [] {new OprofileDaemonEvent()};
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
		this._enabled = enabled;
	}
	
	/**
	 * Method setEvent.
	 * @param event	the event for this counter
	 */
	public void setEvents(OpEvent [] events) {
		OprofileDaemonEvent [] newDaemonEvent = new OprofileDaemonEvent[events.length];
		for (int i = 0; i < events.length; i++) {
			if (i > daemonEvent.length - 1) {
				OprofileDaemonEvent de = new OprofileDaemonEvent();
				de.setEvent(events[i]);
				de.setResetCount(daemonEvent[0].getResetCount());
				newDaemonEvent[i] = de;
			} else {
				daemonEvent[i].setEvent(events[i]);
				newDaemonEvent[i] = daemonEvent[i];
			}
		}
		daemonEvent = newDaemonEvent;
	}
	
	/**
	 * Method setProfileKernel.
	 * @param profileKernel	whether this counter should count kernel events
	 */
	public void setProfileKernel(boolean profileKernel) {
		for (int i = 0; i < daemonEvent.length; i++) {
			daemonEvent[i].setProfileKernel(profileKernel);
		}
	}
	
	/**
	 * Method setProfileUser.
	 * @param profileUser	whether this counter should count user events
	 */
	public void setProfileUser(boolean profileUser) {
		for (int i = 0; i < daemonEvent.length; i++) {
			daemonEvent[i].setProfileUser(profileUser);
		}
	}
	
	/**
	 * Method setCount.
	 * @param count	the number of events between samples for this counter
	 */
	public void setCount(int count) {
		for (int i = 0; i < daemonEvent.length; i++) {
			daemonEvent[i].setResetCount(count);
		}
	}
	
	/**
	 * Saves this counter's configuration into the specified launch
	 * configuration.
	 * @param config	the launch configuration
	 */
	public void saveConfiguration(ILaunchConfigurationWorkingCopy config) {
		config.setAttribute(OprofileLaunchPlugin.ATTR_COUNTER_ENABLED(number), _enabled);
		config.setAttribute(OprofileLaunchPlugin.ATTR_NUMBER_OF_EVENTS(number), daemonEvent.length);

		for (int i = 0; i < daemonEvent.length; i++) {
			if (daemonEvent[i].getEvent() != null) {
				config.setAttribute(OprofileLaunchPlugin.ATTR_COUNTER_EVENT(number, i), daemonEvent[i].getEvent().getText());
				config.setAttribute(OprofileLaunchPlugin.ATTR_COUNTER_UNIT_MASK(number), daemonEvent[i].getEvent().getUnitMask().getMaskValue());
			}
			config.setAttribute(OprofileLaunchPlugin.ATTR_COUNTER_PROFILE_KERNEL(number), daemonEvent[i].getProfileKernel());
			config.setAttribute(OprofileLaunchPlugin.ATTR_COUNTER_PROFILE_USER(number), daemonEvent[i].getProfileUser());
			config.setAttribute(OprofileLaunchPlugin.ATTR_COUNTER_COUNT(number), daemonEvent[i].getResetCount());
		}
	}
	
	/**
	 * Loads a counter configuration from the specified launch configuration.
	 * @param config	the launch configuration
	 */
	public void loadConfiguration(ILaunchConfiguration config) {
		try {
			_enabled = config.getAttribute(OprofileLaunchPlugin.ATTR_COUNTER_ENABLED(number), false);
			int numEvents = config.getAttribute(OprofileLaunchPlugin.ATTR_NUMBER_OF_EVENTS(number), 1);
			daemonEvent = new OprofileDaemonEvent[numEvents];

			for (int i = 0; i < numEvents; i++) {
				String str = config.getAttribute(OprofileLaunchPlugin.ATTR_COUNTER_EVENT(number, i), ""); //$NON-NLS-1$
				int maskValue = config.getAttribute(OprofileLaunchPlugin.ATTR_COUNTER_UNIT_MASK(number), OpUnitMask.SET_DEFAULT_MASK);

				daemonEvent[i] = new OprofileDaemonEvent();
				daemonEvent[i].setEvent(_eventFromString(str));

				if (daemonEvent[i].getEvent() == null) {
					continue;
				}

				daemonEvent[i].getEvent().getUnitMask().setMaskValue(maskValue);
				daemonEvent[i].setProfileKernel(config.getAttribute(OprofileLaunchPlugin.ATTR_COUNTER_PROFILE_KERNEL(number), false));
				daemonEvent[i].setProfileUser(config.getAttribute(OprofileLaunchPlugin.ATTR_COUNTER_PROFILE_USER(number), false));

				daemonEvent[i].setResetCount(config.getAttribute(OprofileLaunchPlugin.ATTR_COUNTER_COUNT(number), OprofileDaemonEvent.COUNT_UNINITIALIZED));
			}
		} catch (CoreException ce) {
			
		}
	}
	
	public OpUnitMask getUnitMask() {
		OpEvent event = daemonEvent[0].getEvent();
		
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
		Object[] args = new Object[] {Integer.valueOf(number)};
		return MessageFormat.format(COUNTER_STRING, args);
	}
	
	/**
	 * Method getNumber.
	 * @return the counter's number
	 */
	public int getNumber() {
		return number;
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
	public OpEvent [] getEvents() {
		List<OpEvent> res = new ArrayList<OpEvent> ();
		for (OprofileDaemonEvent de : daemonEvent) {
			res.add(de.getEvent());
		}
		return res.toArray(new OpEvent[0]);
	}
	
	/**
	 * Method getProfileKernel.
	 * @return whether this counter is counting kernel events
	 */
	public boolean getProfileKernel() {
		return daemonEvent[0].getProfileKernel();
	}
	
	/**
	 * Method getProfileUser.
	 * @return whether this counter is counting user events
	 */
	public boolean getProfileUser() {
		return daemonEvent[0].getProfileUser();
	}

	/**
	 * Method getCount.
	 * @return the number of events between samples for this counter
	 */
	public int getCount() {
		return daemonEvent[0].getResetCount();
	}
	
	/**
	 * Method getValidEvents.
	 * @return an array of all events that this counter can monitor
	 */
	public OpEvent[] getValidEvents() {		
		return eventList;
	}
	
	/**
	 * Gets the daemon event configuration for this counter.
	 * <B>Not</B> valid if this counter is not enabled!
	 * @return the OprofileDaemonEvent
	 */
	public OprofileDaemonEvent [] getDaemonEvents() {
		return daemonEvent;
	}

	// Returns the event with the same label as the parameter STR
	private OpEvent _eventFromString(String str) {
		for (int i = 0; i < eventList.length; i++) {
			if (eventList[i].getText().equals(str))
				return eventList[i];
		}
		
		return null;
	}
}

