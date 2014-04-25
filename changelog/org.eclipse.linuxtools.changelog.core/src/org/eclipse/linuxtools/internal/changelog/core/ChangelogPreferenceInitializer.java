/*******************************************************************************
 * Copyright (c) 2010 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alexander Kurtakov (Red Hat) - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.changelog.core;

import java.net.UnknownHostException;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

public class ChangelogPreferenceInitializer extends
        AbstractPreferenceInitializer {

    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore store = ChangelogPlugin.getDefault()
                .getPreferenceStore();
        store.setDefault("IChangeLogConstants.DATE_FORMAT", "true"); // $NON-NLS-1$
                                                                        // //
                                                                        // $NON-NLS-2$
        store.setDefault("IChangeLogConstants.APPEND_RESOURCE_PATH", "false"); // $NON-NLS-1$
                                                                                // //
                                                                                // $NON-NLS-2$

        store.setDefault("IChangeLogConstants.AUTHOR_NAME", // $NON-NLS-1$
                getUserRealName());
        store.setDefault("IChangeLogConstants.AUTHOR_EMAIL", // $NON-NLS-2$
                getUserEmail());
        store.setDefault("IChangeLogConstants.DEFAULT_FORMATTER", // $NON-NLS-1$
                Messages.getString("ChangeLogPreferencesPage.gnuFormatter")); // $NON-NLS-1$
        store.setDefault("IChangeLogConstants.DEFAULT_EDITOR", // $NON-NLS-1$
                Messages.getString("ChangeLogPreferencesPage.gnuEditorConfig")); // $NON-NLS-1$
    }

    private String getUserRealName() {
        String realUserName = System.getenv("ECLIPSE_CHANGELOG_REALNAME"); // $NON-NLS-1$
        if (realUserName != null)
            return realUserName;
        return System.getProperty("gnu.gcj.user.realname", //$NON-NLS-1$
                getUserName());
    }

    private String getUserEmail() {
        String emailID = System.getenv("ECLIPSE_CHANGELOG_EMAIL"); // $NON-NLS-1$
        if (emailID != null)
            return emailID;
        return getUserName() + "@" + getHostName(); // $NON-NLS-1$
    }

    private String getUserName() {
        return System.getProperty("user.name"); //$NON-NLS-1$
    }

    private String getHostName() {
        try {
            return java.net.InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            // instead of throwing exception, return default host name
            // RH bug#194406
            return "localhost.localdomain"; //$NON-NLS-1$
        }
    }

}
