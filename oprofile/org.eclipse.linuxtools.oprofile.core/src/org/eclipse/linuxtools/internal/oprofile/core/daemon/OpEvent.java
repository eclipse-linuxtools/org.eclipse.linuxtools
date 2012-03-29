/*******************************************************************************
 * Copyright (c) 2004, 2009 Red Hat, Inc.
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

/**
 * A class which represents an Oprofile event
 */
public class OpEvent {
	// The Oprofile event name, i.e., "CPU_CLK_UNHALTED"
	private String _name;
	
	 //  A description of the event
	private String _description;

	// Unit masks for this event type
	private OpUnitMask _unitMask;
	
	// Minimum count
	private int _minCount;
	
	/**
	 * Sets the unit mask for this event.
	 * Only called from XML parsers.
	 * @param mask the new unit mask
	 */
	public void _setUnitMask(OpUnitMask mask) {
		_unitMask = mask;
	}

	/**
	 * Sets the name of this event.
	 * Only called from XML parsers.
	 * @param text the name
	 */
	public void _setText(String text) {
		_name = text;
	}

	/**
	 * Sets the description of this oprofile event.
	 * Only called from XML parsers.
	 * @param text the description
	 */
	public void _setTextDescription(String text) {
		_description = text;
	}

	/**
	 * Sets the minimum count for this event.
	 * Only called from XML parsers.
	 * @param min the minimum count
	 */
	public void _setMinCount(int min) {
		_minCount = min;
	}

	/**
	 * Returns the unit mask corresponding to this event.
	 * @return the unit mask
	 */
	public OpUnitMask getUnitMask() {		
		return _unitMask;
	}
	
	/**
	 * Returns the name of this oprofile event.
	 * @return the name
	 */
	public String getText() {
		return _name;
	}
	
	/**
	 * Returns the description of this oprofile event.
	 * @return the description
	 */
	public String getTextDescription() {
		return _description;
	}
	
	/**
	 * Returns the minimum count allowed for this event.
	 * @return the minimum count
	 */
	public int getMinCount() {
		return _minCount;
	}
}
