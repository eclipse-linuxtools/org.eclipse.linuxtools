/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.systemtap.ui.consolelog.dialogs;

import org.eclipse.osgi.util.NLS;

/**
 * @since 2.0
 */
public class Messages {
	private static final String BUNDLE_NAME = "org.eclipse.linuxtools.systemtap.ui.consolelog.dialogs.messages"; //$NON-NLS-1$
	public static String SelectServerDialog_RemoteServerDetails;
	public static String SelectServerDialog_Host;
	public static String SelectServerDialog_User;
	public static String SelectServerDialog_Password;
	public static String SelectServerDialog_AlwaysConnectToHost;
	public static String SelectServerDialog_Cancel;
	public static String SelectServerDialog_Connect;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}
