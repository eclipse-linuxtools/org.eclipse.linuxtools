/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.logging.preferences;

/**
 * Constant definitions for plug-in preferences
 */
public class PreferenceConstants {

	//environmentvariables
	public static final String[][] P_ENV = new String[][] {
		{"EnvLdLibraryPath", "LD_LIBRARY_PATH"},
		{"EnvPath", "PATH"},
		{"EnvSystemtapTapset", "SYSTEMTAP_TAPSET"},
		{"EnvSystemtapRuntime", "SYSTEMTAP_RUNTIME"},
		//{"EnvDateTime", "DATE_TIME"},
		//{"EnvUser", "USER"},
		//{"EnvKernel", "KERNEL"},
		//{"EnvTestName", "TEST_NAME"},
		//{"EnvSystemtap", "SYSTEMTAP"},
		//{"EnvElfutils", "ELFUTILS"},
		//{"EnvStapObj", "STAP_OBJ"},
		//{"EnvStapSrc", "STAP_SRC"},
		//{"EnvStapInstall", "STAP_INSTALL"},
		//{"EnvStapTests", "STAP_TESTS"},
		//{"EnvCvsroot", "CVSROOT"},
		//{"EnvMailToAddr", "MAIL_TO_ADDR"},
		//{"EnvBuildResults", "BUILD_RESULTS"},
		//{"EnvTestResults", "TEST_RESULTS"},
	};
	
	//Logging
	public static final String P_LOG_ENABLED = "STapLoggingEnabled";
	public static final String P_LOG_TYPE = "STapLoggingType";
	public static final String P_LOG_FILE = "STapLoggingFile";
	public static final String P_LOG_LEVEL = "STapLoggingLevel";
	
	//systemtap
	public static final String P_WINDOW_STATE = "RestoreWindowStatePreference";
}
