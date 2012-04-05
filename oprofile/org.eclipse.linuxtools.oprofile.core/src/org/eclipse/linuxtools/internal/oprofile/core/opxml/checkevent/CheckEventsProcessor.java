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
package org.eclipse.linuxtools.internal.oprofile.core.opxml.checkevent;

import org.eclipse.linuxtools.internal.oprofile.core.opxml.XMLProcessor;

/**
 * XML handler class for opxml's "check-events".
 * @see org.eclipse.linuxtools.internal.oprofile.core.opxml.OpxmlRunner
 */
public class CheckEventsProcessor extends XMLProcessor {
	public static final int INVALID_UNKNOWN = 0;	//unexpected error
	public static final int EVENT_OK = 1;			//valid
	public static final int INVALID_UMASK = 3;		//invalid unit mask value
	public static final int INVALID_COUNTER = 4;	//invalid event for given counter number

	private static final String _RESULT_TAG = "result"; //$NON-NLS-1$
	private static final String _CHECK_EVENTS_TAG ="check-events"; //$NON-NLS-1$

	private static final String _EVENT_OK = "ok"; //$NON-NLS-1$
	private static final String _INVALID_UMASK = "invalid-um"; //$NON-NLS-1$
	private static final String _INVALID_COUNTER = "invalid-counter"; //$NON-NLS-1$

	private int _result;

	/**
	 * @see org.eclipse.linuxtools.internal.oprofile.core.XMLProcessor#reset()
	 */
	public void reset(Object callData) {
		_result = INVALID_UNKNOWN;
	}
	
	/**
	 * @see org.eclipse.linuxtools.internal.oprofile.core.XMLProcessor#endElement(String)
	 */
	public void endElement(String name, Object callData) {
		if (name.equals(_RESULT_TAG)) {
			if (characters.equals(_EVENT_OK)) {
				_result = EVENT_OK;
			} else if (characters.equals(_INVALID_UMASK)) {
				_result = INVALID_UMASK;
			} else if (characters.equals(_INVALID_COUNTER)) {
				_result = INVALID_COUNTER;
			}
		} else if (name.equals(_CHECK_EVENTS_TAG)) {
			int[] result = (int[]) callData;
			result[0] = _result;
		}
	}
}
