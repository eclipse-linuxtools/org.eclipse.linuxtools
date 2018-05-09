/*******************************************************************************
 * Copyright (c) 2013, 2016 Kalray.eu and others
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Xavier Raynaud <xavier.raynaud@kalray.eu> - initial API and implementation
 *    Ingenico - Vincent Guignot <vincent.guignot@ingenico.com> - Add binutils strings
 *******************************************************************************/
package org.eclipse.linuxtools.internal.binutils.preferences;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.binutils.preferences.messages"; //$NON-NLS-1$
    public static String BinutilsPreferencePage_addr2line;
    public static String BinutilsPreferencePage_addr2line_flags;
    public static String BinutilsPreferencePage_cppfilt;
    public static String BinutilsPreferencePage_cppfilt_flags;
    public static String BinutilsPreferencePage_description;
    public static String BinutilsPreferencePage_nm;
    public static String BinutilsPreferencePage_nm_flags;
    public static String BinutilsPreferencePage_title;
    public static String BinutilsPreferencePage_strings;
    public static String BinutilsPreferencePage_strings_flags;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
