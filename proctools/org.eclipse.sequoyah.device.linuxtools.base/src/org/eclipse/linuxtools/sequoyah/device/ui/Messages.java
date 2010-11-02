/********************************************************************************
 * Copyright (c) 2008-2010 Motorola Inc. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributor:
 * Otavio Ferranti (Motorola)
 *
 * Contributors:
 * Daniel Pastore (Eldorado) - [289870] Moving and renaming Tml to Sequoyah
 ********************************************************************************/

package org.eclipse.linuxtools.sequoyah.device.ui;

import org.eclipse.osgi.util.NLS;

/**
 * @author Otavio Ferranti
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.linuxtools.sequoyah.device.ui.messages"; //$NON-NLS-1$
	public static String LoginDialog_Label_Password;
	public static String LoginDialog_Label_User;
	public static String LoginDialog_Msg_Login_Invalid;
	public static String LoginDialog_Window_Message;
	public static String LoginDialog_Window_Title;
	public static String OpenConnectionDialog_Label_Host;
	public static String OpenConnectionDialog_Label_Port;
	public static String OpenConnectionDialog_Label_Protocol;
	public static String OpenConnectionDialog_Window_Message;
	public static String OpenConnectionDialog_Window_Title;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
