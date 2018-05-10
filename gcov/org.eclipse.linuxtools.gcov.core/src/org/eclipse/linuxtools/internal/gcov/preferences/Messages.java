/*******************************************************************************
 * Copyright (c) 2013, 2018 Red Hat Inc.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Alexander Kurtakov <akurtako@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.gcov.preferences;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.gcov.preferences.messages"; //$NON-NLS-1$
    public static String ColorPreferencePage_BackColorHighest;
    public static String ColorPreferencePage_BackColorLowest;
    public static String ColorPreferencePage_BackColorNotCovered;
    public static String ColorPreferencePage_ColorizeCode;
    public static String ColorPreferencePage_Description;
    public static String ColorPreferencePage_Title;
    public static String ColorPreferencePage_UseGradient;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
