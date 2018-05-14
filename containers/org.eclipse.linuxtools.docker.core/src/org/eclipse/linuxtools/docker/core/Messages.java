/*******************************************************************************
 * Copyright (c) 2014, 2018 Red Hat.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.docker.core;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	public static String Default_Name;
	public static String Unnamed;
	public static String Image_Not_Found_Header;
	public static String Image_Pull_Failed_Header;
	public static String Image_Push_Failed_Header;
	public static String Image_Build_Failed_Header;
	public static String Up_specifier;
	public static String Exited_specifier;
	public static String Running_specifier;
	public static String Paused_specifier;
	public static String Removal_In_Progress_specifier;
	public static String Missing_Default_Settings;
	public static String Missing_Settings;
	public static String Retrieve_Default_Settings_Failure;
	public static String Open_Connection;
	public static String Open_Connection_Failure;
	public static String Docker_Daemon_Ping_Failure;
	/**
	 * @since 4.0
	 */
	public static String Docker_Daemon_No_Unix_Socket;
	public static String Retrieve_Docker_Certificates_Failure;
	public static String List_Docker_Containers_Failure;
	public static String List_Docker_Images_Failure;
	public static String Docker_General_Info_Failure;
	public static String Docker_No_Settings_Description_Script;
	public static String Registry_Version_Mismatch;
	public static String Docker_Machine_Command_Not_Found;
	public static String Docker_Connection_Timeout;
	public static String Docker_Machine_Process_Error;
	public static String Docker_Machine_Process_Exception;
	public static String Docker_Compose_Command_Not_Found;
	public static String ImageTagsList_failure;
	public static String ImageTagsList_failure_invalidWwwAuthenticateFormat;

	static {
		// Initialize resource bundle.
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}

	private Messages() {
	}

}
