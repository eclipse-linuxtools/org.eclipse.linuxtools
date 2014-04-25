/* Copyright (c) 2013 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
