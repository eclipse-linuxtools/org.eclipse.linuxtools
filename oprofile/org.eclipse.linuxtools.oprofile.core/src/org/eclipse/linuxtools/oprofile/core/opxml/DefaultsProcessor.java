/*******************************************************************************
 * Copyright (c) 2004 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Keith Seitz <keiths@redhat.com> - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.linuxtools.oprofile.core.opxml;

import org.eclipse.linuxtools.oprofile.core.OpInfo;

/**
 * XML handler class for opxml's "defaults".
 * @see org.eclipse.linuxtools.oprofile.core.opxml.OpxmlRunner
 */
public class DefaultsProcessor extends MapProcessor {
	// XML tags reconize by this processor (public)
	public static final String SAMPLE_DIR = "sample-dir"; //$NON-NLS-1$
	public static final String LOCK_FILE = "lock-file"; //$NON-NLS-1$
	public static final String LOG_FILE = "log-file"; //$NON-NLS-1$
	public static final String DUMP_STATUS = "dump-status"; //$NON-NLS-1$
	
	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.oprofile.core.opxml.XMLProcessor#endElement(java.lang.String, java.lang.Object)
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
