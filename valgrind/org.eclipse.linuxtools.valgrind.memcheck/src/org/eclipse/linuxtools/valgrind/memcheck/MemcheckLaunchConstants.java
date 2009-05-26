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
package org.eclipse.linuxtools.valgrind.memcheck;

public final class MemcheckLaunchConstants {
	// LaunchConfiguration attributes
	public static final String ATTR_MEMCHECK_LEAKRES = MemcheckPlugin.PLUGIN_ID + ".MEMCHECK_LEAKRES"; //$NON-NLS-1$
	public static final String ATTR_MEMCHECK_SHOWREACH = MemcheckPlugin.PLUGIN_ID + ".MEMCHECK_SHOWREACH"; //$NON-NLS-1$
	public static final String ATTR_MEMCHECK_PARTIAL = MemcheckPlugin.PLUGIN_ID + ".MEMCHECK_PARTIAL"; //$NON-NLS-1$
	public static final String ATTR_MEMCHECK_UNDEF = MemcheckPlugin.PLUGIN_ID + ".MEMCHECK_UNDEF"; //$NON-NLS-1$
	public static final String ATTR_MEMCHECK_FREELIST = MemcheckPlugin.PLUGIN_ID + ".MEMCHECK_FREELIST"; //$NON-NLS-1$
	public static final String ATTR_MEMCHECK_GCCWORK = MemcheckPlugin.PLUGIN_ID + ".MEMCHECK_GCCWORK"; //$NON-NLS-1$
	public static final String ATTR_MEMCHECK_ALIGNMENT = MemcheckPlugin.PLUGIN_ID + ".MEMCHECK_ALIGNMENT"; //$NON-NLS-1$
	
	// VG >= 3.4.0
	public static final String ATTR_MEMCHECK_TRACKORIGINS = MemcheckPlugin.PLUGIN_ID + ".MEMCHECK_TRACKORIGINS"; //$NON-NLS-1$
	
	public static final String LEAK_RES_LOW = "low"; //$NON-NLS-1$
	public static final String LEAK_RES_MED = "med"; //$NON-NLS-1$
	public static final String LEAK_RES_HIGH = "high"; //$NON-NLS-1$
	
	public static final String DEFAULT_MEMCHECK_LEAKRES = LEAK_RES_LOW;
	public static final boolean DEFAULT_MEMCHECK_SHOWREACH = false;
	public static final boolean DEFAULT_MEMCHECK_PARTIAL = false;
	public static final boolean DEFAULT_MEMCHECK_UNDEF = true;
	public static final int DEFAULT_MEMCHECK_FREELIST = 10000000;
	public static final boolean DEFAULT_MEMCHECK_GCCWORK = false;
	public static final int DEFAULT_MEMCHECK_ALIGNMENT = 8;
	
	// VG >= 3.4.0
	public static final boolean DEFAULT_MEMCHECK_TRACKORIGINS = false;
}
