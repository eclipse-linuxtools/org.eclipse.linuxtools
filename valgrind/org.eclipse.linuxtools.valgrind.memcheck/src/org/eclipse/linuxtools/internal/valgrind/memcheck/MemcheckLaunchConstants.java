/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.memcheck;

import java.util.Collections;
import java.util.List;

public final class MemcheckLaunchConstants {
	// LaunchConfiguration attributes
	public static final String ATTR_MEMCHECK_LEAKCHECK = MemcheckPlugin.PLUGIN_ID + ".MEMCHECK_LEAKCHECK"; //$NON-NLS-1$
	public static final String ATTR_MEMCHECK_LEAKRES = MemcheckPlugin.PLUGIN_ID + ".MEMCHECK_LEAKRES"; //$NON-NLS-1$
	public static final String ATTR_MEMCHECK_SHOWREACH = MemcheckPlugin.PLUGIN_ID + ".MEMCHECK_SHOWREACH"; //$NON-NLS-1$
	public static final String ATTR_MEMCHECK_PARTIAL = MemcheckPlugin.PLUGIN_ID + ".MEMCHECK_PARTIAL"; //$NON-NLS-1$
	public static final String ATTR_MEMCHECK_UNDEF = MemcheckPlugin.PLUGIN_ID + ".MEMCHECK_UNDEF"; //$NON-NLS-1$
	public static final String ATTR_MEMCHECK_FREELIST = MemcheckPlugin.PLUGIN_ID + ".MEMCHECK_FREELIST"; //$NON-NLS-1$
	public static final String ATTR_MEMCHECK_GCCWORK = MemcheckPlugin.PLUGIN_ID + ".MEMCHECK_GCCWORK"; //$NON-NLS-1$
	public static final String ATTR_MEMCHECK_ALIGNMENT_BOOL = MemcheckPlugin.PLUGIN_ID + ".MEMCHECK_ALIGNMENT_BOOL"; //$NON-NLS-1$
	public static final String ATTR_MEMCHECK_ALIGNMENT_VAL = MemcheckPlugin.PLUGIN_ID + ".MEMCHECK_ALIGNMENT_VAL"; //$NON-NLS-1$
	public static final String ATTR_MEMCHECK_MALLOCFILL_BOOL = MemcheckPlugin.PLUGIN_ID + ".MEMCHECK_MALLOCFILL_BOOL"; //$NON-NLS-1$
	public static final String ATTR_MEMCHECK_MALLOCFILL_VAL = MemcheckPlugin.PLUGIN_ID + ".MEMCHECK_MALLOCFILL_VAL"; //$NON-NLS-1$
	public static final String ATTR_MEMCHECK_FREEFILL_BOOL = MemcheckPlugin.PLUGIN_ID + ".MEMCHECK_FREEFILL_BOOL"; //$NON-NLS-1$
	public static final String ATTR_MEMCHECK_FREEFILL_VAL = MemcheckPlugin.PLUGIN_ID + ".MEMCHECK_FREEFILL_VAL"; //$NON-NLS-1$
	public static final String ATTR_MEMCHECK_IGNORE_RANGES = MemcheckPlugin.PLUGIN_ID + ".MEMCHECK_IGNORE_RANGES"; //$NON-NLS-1$
	
	// VG >= 3.4.0
	public static final String ATTR_MEMCHECK_TRACKORIGINS = MemcheckPlugin.PLUGIN_ID + ".MEMCHECK_TRACKORIGINS"; //$NON-NLS-1$
	
	// VG >= 3.6.0
	public static final String ATTR_MEMCHECK_POSSIBLY_LOST_BOOL = MemcheckPlugin.PLUGIN_ID + ".MEMCHECK_POSSIBLY_LOST"; //$NON-NLS-1$

	public static final String LEAK_RES_LOW = "low"; //$NON-NLS-1$
	public static final String LEAK_RES_MED = "med"; //$NON-NLS-1$
	public static final String LEAK_RES_HIGH = "high"; //$NON-NLS-1$
	
	public static final boolean DEFAULT_MEMCHECK_LEAKCHECK = true;
	public static final String DEFAULT_MEMCHECK_LEAKRES = LEAK_RES_HIGH;
	public static final boolean DEFAULT_MEMCHECK_SHOWREACH = false;
	public static final boolean DEFAULT_MEMCHECK_PARTIAL = false;
	public static final boolean DEFAULT_MEMCHECK_UNDEF = true;
	public static final int DEFAULT_MEMCHECK_FREELIST = 10000000;
	public static final boolean DEFAULT_MEMCHECK_GCCWORK = false;
	public static final boolean DEFAULT_MEMCHECK_ALIGNMENT_BOOL = false;
	public static final int DEFAULT_MEMCHECK_ALIGNMENT_VAL = 0;
	public static final boolean DEFAULT_MEMCHECK_MALLOCFILL_BOOL = false;
	public static final String DEFAULT_MEMCHECK_MALLOCFILL_VAL = "";
	public static final boolean DEFAULT_MEMCHECK_FREEFILL_BOOL = false;
	public static final String DEFAULT_MEMCHECK_FREEFILL_VAL = "";
	public static final List<String> DEFAULT_MEMCHECK_IGNORE_RANGES = Collections.emptyList();
	
	// VG >= 3.4.0
	public static final boolean DEFAULT_MEMCHECK_TRACKORIGINS = false;
	
	// VG >= 3.6.0
	public static final boolean DEFAULT_MEMCHECK_POSSIBLY_LOST_BOOL = false;
}
