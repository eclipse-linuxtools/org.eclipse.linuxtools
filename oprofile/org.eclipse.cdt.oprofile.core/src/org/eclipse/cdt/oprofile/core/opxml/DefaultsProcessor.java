/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/
package org.eclipse.cdt.oprofile.core.opxml;

import org.eclipse.cdt.oprofile.core.OpInfo;

/**
 * XML handler class for opxml's "defaults".
 * @see org.eclipse.cdt.oprofile.core.opxml.OpxmlRunner
 * @author Keith Seitz <keiths@redhat.com>
 */
public class DefaultsProcessor extends MapProcessor {
	// XML tags reconize by this processor (public)
	public static final String SAMPLE_DIR = "sample-dir"; //$NON-NLS-1$
	public static final String LOCK_FILE = "lock-file"; //$NON-NLS-1$
	public static final String LOG_FILE = "log-file"; //$NON-NLS-1$
	public static final String DUMP_STATUS = "dump-status"; //$NON-NLS-1$
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.oprofile.core.opxml.XMLProcessor#endElement(java.lang.String, java.lang.Object)
	 */
	public void endElement(String name, Object callData) {
		if (name.equals(OpInfoProcessor.DEFAULTS_TAG)) {
			OpInfo info = (OpInfo) callData;
			info.setDefaults(_map);
			OprofileSAXHandler.getInstance(callData).pop(name);
		} else {
			super.endElement(name, callData);
		}
	}
}
