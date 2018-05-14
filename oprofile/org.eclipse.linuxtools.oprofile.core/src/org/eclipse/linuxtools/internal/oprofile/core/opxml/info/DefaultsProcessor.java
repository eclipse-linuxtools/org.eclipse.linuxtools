/*******************************************************************************
 * Copyright (c) 2004, 2018 Red Hat, Inc.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Keith Seitz <keiths@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.oprofile.core.opxml.info;

import java.util.HashMap;

import org.eclipse.linuxtools.internal.oprofile.core.daemon.OpInfo;
import org.eclipse.linuxtools.internal.oprofile.core.opxml.OprofileSAXHandler;
import org.eclipse.linuxtools.internal.oprofile.core.opxml.XMLProcessor;

/**
 * XML handler class for opxml's "defaults".
 * 
 * @see org.eclipse.linuxtools.internal.oprofile.core.opxml.OpxmlRunner
 */
public class DefaultsProcessor extends XMLProcessor {
	// XML tags reconize by this processor (public)
	public static final String SAMPLE_DIR = "sample-dir"; //$NON-NLS-1$
	public static final String LOCK_FILE = "lock-file"; //$NON-NLS-1$
	public static final String LOG_FILE = "log-file"; //$NON-NLS-1$
	public static final String DUMP_STATUS = "dump-status"; //$NON-NLS-1$
	protected HashMap<String, String> map = new HashMap<>();

	@Override
	public void reset(Object callData) {
		map.clear();
	}

	@Override
	public void endElement(String name, Object callData) {
		if (name.equals(OpInfoProcessor.DEFAULTS_TAG)) {
			OpInfo info = (OpInfo) callData;
			info.setDefaults(map);
			OprofileSAXHandler.getInstance(callData).pop(name);
		} else {
			map.put(name, characters);
		}
	}
}
