/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/
package org.eclipse.cdt.oprofile.core.opxml;

/**
 * A class of constants for communications with the opxml wrapper
 * program.
 * @author Keith Seitz <keiths@redhat.com>
 * @see org.eclipse.cdt.oprofile.core.opxml.OprofileSAXHandler
 */
public class OpxmlConstants {
	/**
	 * Request static oprofile information (num counters, defaults, event lists) 
	 */
	public static final String OPXML_INFO = "info"; //$NON-NLS-1$
	public static final String INFO_TAG = OPXML_INFO;

	/**
	 * Request samples for a session
	 */
	public static final String OPXML_SAMPLES = "samples"; //$NON-NLS-1$
	public static final String SAMPLES_TAG = OPXML_SAMPLES;

	/**
	 * Request debug info for ???
	 */
	public static final String OPXML_DEBUGINFO = "debug-info"; //$NON-NLS-1$
	public static final String DEBUGINFO_TAG = OPXML_DEBUGINFO;
	
	/**
	 * Request event validity check
	 */
	public static final String OPXML_CHECKEVENTS = "check-events"; //$NON-NLS-1$
	public static final String CHECKEVENTS_TAG = OPXML_CHECKEVENTS;
	
	/**
	 * Request session list
	 */
	public static final String OPXML_SESSIONS  = "sessions"; //$NON-NLS-1$
	public static final String SESSIONS_TAG = OPXML_SESSIONS;
}
