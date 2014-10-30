/*******************************************************************************
 * Copyright (c) 2008, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.launch;

import java.util.Collections;
import java.util.List;


public final class LaunchConfigurationConstants {

    // LaunchConfiguration constants
    private static final String PLUGIN_ID = "org.eclipse.linuxtools.valgrind.launch"; //$NON-NLS-1$

    public static final String ATTR_TOOL = PLUGIN_ID + ".TOOL"; //$NON-NLS-1$
    public static final String ATTR_INTERNAL_OUTPUT_DIR = PLUGIN_ID + ".OUTPUT_DIR"; //$NON-NLS-1$

    public static final String ATTR_GENERAL_TRACECHILD = PLUGIN_ID + ".GENERAL_TRACECHILD"; //$NON-NLS-1$
    public static final String ATTR_GENERAL_CHILDSILENT = PLUGIN_ID + ".GENERAL_CHILDSILENT"; //$NON-NLS-1$
    public static final String ATTR_GENERAL_FREERES = PLUGIN_ID + ".GENERAL_FREERES"; //$NON-NLS-1$
    public static final String ATTR_GENERAL_DEMANGLE = PLUGIN_ID + ".GENERAL_DEMANGLE"; //$NON-NLS-1$
    public static final String ATTR_GENERAL_NUMCALLERS = PLUGIN_ID + ".GENERAL_NUMCALLERS"; //$NON-NLS-1$
    public static final String ATTR_GENERAL_ERRLIMIT = PLUGIN_ID + ".GENERAL_ERRLIMIT"; //$NON-NLS-1$
    public static final String ATTR_GENERAL_BELOWMAIN = PLUGIN_ID + ".GENERAL_BELOWMAIN"; //$NON-NLS-1$
    public static final String ATTR_GENERAL_MAXFRAME = PLUGIN_ID + ".GENERAL_MAXFRAME"; //$NON-NLS-1$
    public static final String ATTR_GENERAL_SUPPFILES = PLUGIN_ID + ".GENERAL_SUPPFILES"; //$NON-NLS-1$

    // 3.4.0 specific
    public static final String ATTR_GENERAL_MAINSTACK_BOOL = PLUGIN_ID + ".GENERAL_MAINSTACK_BOOL"; //$NON-NLS-1$
    public static final String ATTR_GENERAL_MAINSTACK = PLUGIN_ID + ".GENERAL_MAINSTACK"; //$NON-NLS-1$

    // 3.6.0 specific
    public static final String ATTR_GENERAL_DSYMUTIL = PLUGIN_ID + ".GENERAL_DSYMUTIL"; //$NON-NLS-1$
    public static final String ATTR_FULLPATH_AFTER = PLUGIN_ID + ".FULLPATH_AFTER"; //$NON-NLS-1$

    public static final String DEFAULT_TOOL = PLUGIN_ID + ".memcheck"; //$NON-NLS-1$

    public static final boolean DEFAULT_GENERAL_TRACECHILD = false;
    public static final boolean DEFAULT_GENERAL_CHILDSILENT = true; // necessary for parsing
    public static final boolean DEFAULT_GENERAL_FREERES = true;
    public static final boolean DEFAULT_GENERAL_DEMANGLE = true;
    public static final int DEFAULT_GENERAL_NUMCALLERS = 12;
    public static final boolean DEFAULT_GENERAL_ERRLIMIT = true;
    public static final boolean DEFAULT_GENERAL_BELOWMAIN = false;
    public static final int DEFAULT_GENERAL_MAXFRAME = 2000000;
    public static final List<String> DEFAULT_GENERAL_SUPPFILES = Collections.emptyList();

    // 3.4.0 specific
    public static final boolean DEFAULT_GENERAL_MAINSTACK_BOOL = false;
    public static final int DEFAULT_GENERAL_MAINSTACK = 0;

    // 3.6.0 specific
    public static final boolean DEFAULT_GENERAL_DSYMUTIL = true; // necessary for OS X, Bug #333640
    public static final boolean DEFAULT_FULLPATH_AFTER = true;
}
