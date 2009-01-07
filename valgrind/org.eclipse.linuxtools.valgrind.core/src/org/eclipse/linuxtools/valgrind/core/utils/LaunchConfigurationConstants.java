/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.valgrind.core.utils;

public final class LaunchConfigurationConstants {

	// LaunchConfiguration constants
	private static final String PLUGIN_ID = "org.eclipse.linuxtools.valgrind.launch"; //$NON-NLS-1$
	
	public static final String ATTR_TOOL = PLUGIN_ID + ".TOOL"; //$NON-NLS-1$

	public static final String ATTR_GENERAL_TRACECHILD = PLUGIN_ID + ".GENERAL_TRACECHILD"; //$NON-NLS-1$
	public static final String ATTR_GENERAL_CHILDSILENT = PLUGIN_ID + ".GENERAL_CHILDSILENT"; //$NON-NLS-1$
	//	public static final String ATTR_GENERAL_TRACKFDS = PLUGIN_ID + ".GENERAL_TRACKFDS";
	//	public static final String ATTR_GENERAL_TIMESTAMP = PLUGIN_ID + ".GENERAL_TIMESTAMP";
	public static final String ATTR_GENERAL_FREERES = PLUGIN_ID + ".GENERAL_FREERES"; //$NON-NLS-1$
	public static final String ATTR_GENERAL_DEMANGLE = PLUGIN_ID + ".GENERAL_DEMANGLE"; //$NON-NLS-1$

	public static final String ATTR_GENERAL_NUMCALLERS = PLUGIN_ID + ".GENERAL_NUMCALLERS"; //$NON-NLS-1$
	public static final String ATTR_GENERAL_ERRLIMIT = PLUGIN_ID + ".GENERAL_ERRLIMIT"; //$NON-NLS-1$
	public static final String ATTR_GENERAL_BELOWMAIN = PLUGIN_ID + ".GENERAL_BELOWMAIN"; //$NON-NLS-1$
	public static final String ATTR_GENERAL_MAXFRAME = PLUGIN_ID + ".GENERAL_MAXFRAME"; //$NON-NLS-1$
	public static final String ATTR_GENERAL_SUPPFILE = PLUGIN_ID + ".GENERAL_SUPPFILE"; //$NON-NLS-1$

}
