/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/
package org.eclipse.cdt.oprofile.core.opxml;

/**
 * XML handler class for opxml's "check-events".
 * @see org.eclipse.cdt.oprofile.core.opxml.OpxmlRunner
 * @author Keith Seitz <keiths@redhat.com>
 */
public class CheckEventsProcessor extends XMLProcessor {
	// A flag of all the possible problems with the 
	private int _result;
	
	/**
	 * The checked event is valid.
	 */
	public static final int EVENT_OK = 0;
	
	/**
	 * The checked event has an invalid event.
	 */
	public static final int INVALID_EVENT = 1;

	/**
	 * The checked event has an invalid unit mask.
	 */
	public static final int INVALID_UMASK = 2;
	
	/**
	 * The checked event is for an invalid counter.
	 */
	public static final int INVALID_COUNTER = 4;
	
	private static final String _RESULT_TAG = "result"; //$NON-NLS-1$
	private static final String _CHECK_EVENTS_TAG ="check-events"; //$NON-NLS-1$

	private static final String _INVALID_EVENT = "invalid-event"; //$NON-NLS-1$
	private static final String _INVALID_UMASK = "invalid-um"; //$NON-NLS-1$
	private static final String _INVALID_COUNTER = "invalid-counter"; //$NON-NLS-1$
	
	/**
	 * @see org.eclipse.cdt.oprofile.core.XMLProcessor#reset()
	 */
	public void reset(Object callData) {
		_result = EVENT_OK;
	}
	
	/**
	 * @see org.eclipse.cdt.oprofile.core.XMLProcessor#endElement(String)
	 */
	public void endElement(String name, Object callData) {
		if (name.equals(_RESULT_TAG)) {
			int flag = 0;
			if (_characters.equals(_INVALID_EVENT)) {
				flag = INVALID_EVENT;
			} else if (_characters.equals(_INVALID_UMASK)) {
				flag = INVALID_UMASK;
			} else if (_characters.equals(_INVALID_COUNTER)) {
				flag = INVALID_COUNTER;
			}
			
			_result |= flag;
		} else if (name.equals(_CHECK_EVENTS_TAG)) {
			int[] result = (int[]) callData;
			result[0] = _result;
		}
	}
}
