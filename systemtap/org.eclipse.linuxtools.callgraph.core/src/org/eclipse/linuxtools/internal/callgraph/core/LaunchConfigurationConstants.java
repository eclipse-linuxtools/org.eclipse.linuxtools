/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.callgraph.core;


/**
 *  Contains the name and default value of configuration variables, as well as
 *  a few other plugin-specific constants.
 *
 */
public final class LaunchConfigurationConstants {


    //Configuration type variables
    private static final String INVALID = ""; //$NON-NLS-1$
    public static final String PLUGIN_ID = "org.eclipse.linuxtools.callgraph.launch"; //$NON-NLS-1$

    public static final String COMMAND_VERBOSE = PLUGIN_ID + ".COMMAND_VERBOSE"; //$NON-NLS-1$
    public static final String COMMAND_PASS = PLUGIN_ID + ".PASS"; //$NON-NLS-1$
    public static final String COMMAND_KEEP_TEMPORARY = PLUGIN_ID + ".KEEP_TEMPORARY"; //$NON-NLS-1$
    public static final String COMMAND_GURU = PLUGIN_ID + ".GURU"; //$NON-NLS-1$
    public static final String COMMAND_PROLOGUE_SEARCH = PLUGIN_ID + ".PROLOGUE_SEARCH"; //$NON-NLS-1$
    public static final String COMMAND_NO_CODE_ELISION = PLUGIN_ID + ".NO_CODE_ELISION"; //$NON-NLS-1$
    public static final String COMMAND_DISABLE_WARNINGS = PLUGIN_ID + ".DISABLE_WARNINGS"; //$NON-NLS-1$
    public static final String COMMAND_BULK_MODE = PLUGIN_ID + ".BULK_MODE"; //$NON-NLS-1$
    public static final String COMMAND_TIMING_INFO = PLUGIN_ID + ".TIMING_INFO"; //$NON-NLS-1$
    public static final String COMMAND_SKIP_BADVARS = PLUGIN_ID + ".SKIP_BADVARS"; //$NON-NLS-1$
    public static final String COMMAND_IGNORE_DWARF = PLUGIN_ID + ".IGNORE_DWARF"; //$NON-NLS-1$
    public static final String COMMAND_TAPSET_COVERAGE = PLUGIN_ID + ".TAPSET_COVERAGE"; //$NON-NLS-1$
    public static final String BINARY_PATH = PLUGIN_ID + ".BINARY_PATH";  //$NON-NLS-1$
    public static final String SCRIPT_PATH = PLUGIN_ID + ".SCRIPT_PATH"; //$NON-NLS-1$
    public static final String COMMAND_LEAVE_RUNNING = PLUGIN_ID + ".LEAVE_RUNNING"; //$NON-NLS-1$
    public static final String COMMAND_C_DIRECTIVES = PLUGIN_ID + ".C_DIRECTIVES"; //$NON-NLS-1$
    public static final String COMMAND_BUFFER_BYTES = PLUGIN_ID + ".BUFFER_BYTES"; //$NON-NLS-1$
    public static final String COMMAND_TARGET_PID = PLUGIN_ID + ".TARGET_PID"; //$NON-NLS-1$
    public static final String ARGUMENTS = PLUGIN_ID + ".ARGUMENTS"; //$NON-NLS-1$
    public static final String NUMBER_OF_ARGUMENTS = PLUGIN_ID + ".NUMBER_OF_ARGUMENTS"; //$NON-NLS-1$
    public static final String OUTPUT_PATH = PLUGIN_ID + ".OUTPUT_PATH";  //$NON-NLS-1$
    public static final String OVERWRITE = PLUGIN_ID + ".OVERWRITE";  //$NON-NLS-1$
    public static final String BUILD_PROJECT = PLUGIN_ID + ".BUILD_PROJECT"; //$NON-NLS-1$
    public static final String COMMAND_LIST = PLUGIN_ID + ".COMMAND_LIST"; //$NON-NLS-1$
    public static final String BINARY_ARGUMENTS = PLUGIN_ID + ".BINARY_ARGUMENTS"; //$NON-NLS-1$
    public static final String PARSER_CLASS = PLUGIN_ID + ".PARSER_CLASS"; //$NON-NLS-1$
    public static final String VIEW_CLASS = PLUGIN_ID + ".VIEW_CLASS";   //$NON-NLS-1$
    public static final String SECONDARY_VIEW_ID = PLUGIN_ID + ".SECONDARY_VIEW_ID"; //$NON-NLS-1$


    public static final String GENERATED_SCRIPT = PLUGIN_ID + ".GENERATED_SCRIPT"; //$NON-NLS-1$
    public static final String NEED_TO_GENERATE =  PLUGIN_ID + ".NEED_TO_GENERATE"; //$NON-NLS-1$
    public static final String USE_COLOUR = PLUGIN_ID + ".USE_COLOUR"; //$NON-NLS-1$
    public static final String COMMAND = ".COMMAND"; //$NON-NLS-1$

    //Defaults
    public static final int DEFAULT_COMMAND_VERBOSE = 0;
    public static final int DEFAULT_COMMAND_PASS = 0;
    public static final boolean DEFAULT_COMMAND_KEEP_TEMPORARY = false;
    public static final boolean DEFAULT_COMMAND_GURU = false;
    public static final boolean DEFAULT_COMMAND_PROLOGUE_SEARCH = false;
    public static final boolean DEFAULT_COMMAND_NO_CODE_ELISION = false;
    public static final boolean DEFAULT_COMMAND_DISABLE_WARNINGS = false;
    public static final boolean DEFAULT_COMMAND_BULK_MODE = false;
    public static final boolean DEFAULT_COMMAND_TIMING_INFO = false;
    public static final boolean DEFAULT_COMMAND_SKIP_BADVARS = false;
    public static final boolean DEFAULT_COMMAND_IGNORE_DWARF = false;
    public static final boolean DEFAULT_COMMAND_TAPSET_COVERAGE = false;
    public static final String DEFAULT_BINARY_PATH = INVALID;
    public static final String DEFAULT_SCRIPT_PATH = INVALID;
    public static final boolean DEFAULT_COMMAND_LEAVE_RUNNING = false;
    public static final String DEFAULT_COMMAND_C_DIRECTIVES = INVALID;
    public static final int DEFAULT_COMMAND_BUFFER_BYTES = 0;
    public static final int DEFAULT_COMMAND_TARGET_PID = 0;
    public static final String DEFAULT_ARGUMENTS = INVALID;
    public static final String DEFAULT_OUTPUT_PATH = INVALID;
    public static final boolean DEFAULT_OVERWRITE = false;
    public static final int DEFAULT_NUMBER_OF_ARGUMENTS = 0;
    public static final boolean DEFAULT_BUILD_PROJECT = true;
    public static final String DEFAULT_COMMAND_LIST = INVALID;
    public static final String DEFAULT_BINARY_ARGUMENTS = INVALID;

    public static final String DEFAULT_GENERATED_SCRIPT = INVALID;
    public static final boolean DEFAULT_NEED_TO_GENERATE = false;
    public static final boolean DEFAULT_USE_COLOUR = false;

    public static final String DEFAULT_PARSER_CLASS = INVALID;
    public static final String DEFAULT_VIEW_CLASS = INVALID;
    public static final String DEFAULT_SECONDARY_VIEW_ID = INVALID;

}
