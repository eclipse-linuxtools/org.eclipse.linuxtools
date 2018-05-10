/******************************************************************************
 * Copyright (c) 2013, 2018 Red Hat Inc.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alexander Kurtakov - Initial API and implementation.
 *******************************************************************************/
package org.eclipse.linuxtools.internal.systemtap.ui.consolelog.preferences;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.systemtap.ui.consolelog.preferences.messages"; //$NON-NLS-1$
    public static String ConsoleLogPreferencePage_AlwaysConnectToHost;
    public static String ConsoleLogPreferencePage_Host;
    public static String ConsoleLogPreferencePage_Password;
    public static String ConsoleLogPreferencePage_Port;
    public static String ConsoleLogPreferencePage_PreferencesTitle;
    public static String ConsoleLogPreferencePage_SecondsToSaveData;
    public static String ConsoleLogPreferencePage_User;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
