/*******************************************************************************
 * Copyright (c) 2007 Alphonse Van Assche.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alphonse Van Assche - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.rpm.ui.editor.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.linuxtools.internal.rpm.ui.editor.Activator;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

    /*
     * (non-Javadoc)
     *
     * @seeorg.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#
     * initializeDefaultPreferences()
     */
    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        store.setDefault(PreferenceConstants.P_CURRENT_RPMTOOLS,
                PreferenceConstants.DP_RPMTOOLS_RPM);
        store.setDefault(PreferenceConstants.P_RPM_LIST_FILEPATH,
                PreferenceConstants.DP_RPM_LIST_FILEPATH);
        store.setDefault(PreferenceConstants.P_RPM_LIST_MAX_PROPOSALS,
                PreferenceConstants.DP_RPM_LIST_MAX_PROPOSALS);
        store.setDefault(PreferenceConstants.P_RPM_LIST_BACKGROUND_BUILD,
                PreferenceConstants.DP_RPM_LIST_BACKGROUND_BUILD);
        store.setDefault(PreferenceConstants.P_RPM_LIST_BUILD_PERIOD,
                PreferenceConstants.DP_RPM_LIST_BUILD_PERIOD);
        store.setDefault(PreferenceConstants.P_MACRO_PROPOSALS_FILESPATH,
                PreferenceConstants.DP_MACRO_PROPOSALS_FILESPATH);
        store.setDefault(PreferenceConstants.P_CHANGELOG_LOCAL,
                PreferenceConstants.DP_CHANGELOG_LOCAL);
        store.setDefault(PreferenceConstants.P_CHANGELOG_ENTRY_FORMAT,
                PreferenceConstants.DP_CHANGELOG_ENTRY_FORMAT);
        store.setDefault(PreferenceConstants.P_MACRO_HOVER_CONTENT,
                PreferenceConstants.P_MACRO_HOVER_CONTENT_VIEWDESCRIPTION);
        store.setDefault(PreferenceConstants.P_RPMINFO_NAME,
                PreferenceConstants.DP_RPMINFO_NAME);
        store.setDefault(PreferenceConstants.P_RPMINFO_VERSION,
                PreferenceConstants.DP_RPMINFO_VERSION);
        store.setDefault(PreferenceConstants.P_RPMINFO_RELEASE,
                PreferenceConstants.DP_RPMINFO_RELEASE);
        store.setDefault(PreferenceConstants.P_RPMINFO_SUMMARY,
                PreferenceConstants.DP_RPMINFO_SUMMARY);
        store.setDefault(PreferenceConstants.P_RPMINFO_LICENSE,
                PreferenceConstants.DP_RPMINFO_LICENSE);
        store.setDefault(PreferenceConstants.P_RPMINFO_GROUP,
                PreferenceConstants.DP_RPMINFO_GROUP);
        store.setDefault(PreferenceConstants.P_RPMINFO_URL,
                PreferenceConstants.DP_RPMINFO_URL);
        store.setDefault(PreferenceConstants.P_RPMINFO_DESCRIPTION,
                PreferenceConstants.DP_RPMINFO_DESCRIPTION);
        store.setDefault(PreferenceConstants.P_RPMINFO_INSTALLTIME,
                PreferenceConstants.DP_RPMINFO_INSTALLTIME);
        store.setDefault(PreferenceConstants.P_RPMINFO_SIZE,
                PreferenceConstants.DP_RPMINFO_SIZE);
        store.setDefault(PreferenceConstants.P_RPMINFO_PACKAGER,
                PreferenceConstants.DP_RPMINFO_PACKAGER);
        store.setDefault(PreferenceConstants.P_RPMINFO_VENDOR,
                PreferenceConstants.DP_RPMINFO_VENDOR);
        store.setDefault(PreferenceConstants.P_RPMINFO_BUILDTIME,
                PreferenceConstants.DP_RPMINFO_BUILDTIME);
        store.setDefault(PreferenceConstants.P_RPMINFO_SOURCERPM,
                PreferenceConstants.DP_RPMINFO_SOURCERPM);
        store.setDefault(PreferenceConstants.P_TASK_TAGS,
                PreferenceConstants.DP_TASK_TAGS);
        // Convert tab by spaces
        store.setDefault(PreferenceConstants.P_SPACES_FOR_TABS,
                PreferenceConstants.DP_SPACES_FOR_TABS);
        store.setDefault(PreferenceConstants.P_NBR_OF_SPACES_FOR_TAB,
                PreferenceConstants.DP_NBR_OF_SPACES_FOR_TAB);

    }

}
