/*******************************************************************************
 * Copyright (c) 2014 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.docker.core;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	public static String Default_Name;
	public static String Image_Not_Found_Header;
	public static String Image_Pull_Failed_Header;
	public static String Image_Push_Failed_Header;
	public static String Image_Build_Failed_Header;
	public static String Up_specifier;
	public static String Exited_specifier;
	public static String Running_specifier;
	public static String Paused_specifier;
	public static String Missing_Default_Settings;
	public static String Missing_Settings;
	public static String Retrieve_Default_Settings_Failure;
	public static String Open_Connection_Failure;
	public static String Docker_Daemon_Ping_Failure;
	public static String Retrieve_Docker_Certificates_Failure;
	public static String List_Docker_Containers_Failure;
	public static String Docker_General_Info_Failure;
	public static String Docker_No_Settings_Description_Script;
	public static String Registry_Version_Mismatch;

	static {
		// Initialize resource bundle.
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}

	private Messages() {
	}

}
