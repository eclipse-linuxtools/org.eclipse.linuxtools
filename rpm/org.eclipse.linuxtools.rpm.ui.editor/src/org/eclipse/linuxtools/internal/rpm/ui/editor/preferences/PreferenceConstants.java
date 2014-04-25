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

/**
 * Constant definitions for plug-in preferences
 */
public final class PreferenceConstants {

    /*
     * Other Constant
     */
    public static final String RPMMACRO_FILE = System.getProperty("user.home") + "/.rpmmacros"; //$NON-NLS-1$//$NON-NLS-2$

    /*
     * Prefences keys
     */

    // rpm list builder
    public static final String P_RPM_LIST_FILEPATH = "rpmListFilePath"; //$NON-NLS-1$
    public static final String P_RPM_LIST_MAX_PROPOSALS = "rpmListMaxProposalsInfo"; //$NON-NLS-1$
    public static final String P_CURRENT_RPMTOOLS = "currentRpmtools"; //$NON-NLS-1$
    public static final String P_RPM_LIST_BACKGROUND_BUILD = "rpmListBackgroundBuild"; //$NON-NLS-1$
    public static final String P_RPM_LIST_BUILD_PERIOD = "rpmListBuildPeriod"; //$NON-NLS-1$
    public static final String P_RPM_LIST_LAST_BUILD = "rpmLisMastBuild"; //$NON-NLS-1$
    // macro
    public static final String P_MACRO_PROPOSALS_FILESPATH = "macroProposalsFilespath"; //$NON-NLS-1$
    public static final String P_MACRO_HOVER_CONTENT_VIEWCONTENT = "macroHoverViewContent"; //$NON-NLS-1$
    public static final String P_MACRO_HOVER_CONTENT_VIEWDESCRIPTION = "macroHoverViewDescription"; //$NON-NLS-1$
    public static final String P_MACRO_HOVER_CONTENT = P_MACRO_HOVER_CONTENT_VIEWCONTENT;
    // changelog
    public static final String P_CHANGELOG_LOCAL = "changelogLocal"; //$NON-NLS-1$

    public static final String P_CHANGELOG_ENTRY_FORMAT = "changelogEntryFormat"; //$NON-NLS-1$
    public static final String P_CHANGELOG_ENTRY_FORMAT_VERSIONED = "1"; //$NON-NLS-1$
    public static final String P_CHANGELOG_ENTRY_FORMAT_VERSIONED_WITH_SEPARATOR = "2"; //$NON-NLS-1$
    public static final String P_CHANGELOG_ENTRY_FORMAT_UNVERSIONED = "3"; //$NON-NLS-1$

    // RPM informations
    public static final String P_RPMINFO_NAME = "RpmInfoName"; //$NON-NLS-1$
    public static final String P_RPMINFO_VERSION = "RpmInfoVersion"; //$NON-NLS-1$
    public static final String P_RPMINFO_RELEASE = "RpmInfoRelease"; //$NON-NLS-1$
    public static final String P_RPMINFO_SUMMARY = "RpmInfoSummary"; //$NON-NLS-1$
    public static final String P_RPMINFO_LICENSE = "RpmInfoLicense"; //$NON-NLS-1$
    public static final String P_RPMINFO_GROUP = "RpmInfoName"; //$NON-NLS-1$
    public static final String P_RPMINFO_URL = "RpmInfoUrl"; //$NON-NLS-1$
    public static final String P_RPMINFO_DESCRIPTION = "RpmInfoDescription"; //$NON-NLS-1$
    public static final String P_RPMINFO_INSTALLTIME = "RpmInfoInstallDate"; //$NON-NLS-1$
    public static final String P_RPMINFO_SIZE = "RpmInfoSize"; //$NON-NLS-1$
    public static final String P_RPMINFO_PACKAGER = "RpmInfoPackager"; //$NON-NLS-1$
    public static final String P_RPMINFO_VENDOR = "RpmInfoVendor"; //$NON-NLS-1$
    public static final String P_RPMINFO_BUILDTIME = "RpmInfoBuildDate"; //$NON-NLS-1$
    public static final String P_RPMINFO_SOURCERPM = "RpmInfoSourceRpm"; //$NON-NLS-1$
    public static final String P_TASK_TAGS = "RpmSpecTaskTags"; //$NON-NLS-1$
    // Convert tab by spaces
    public static final String P_SPACES_FOR_TABS = "SpacesForTabs"; //$NON-NLS-1$
    public static final String P_NBR_OF_SPACES_FOR_TAB = "NbrOfSpacesForTab"; //$NON-NLS-1$

    /*
     * Preferences default values
     */

    public static final String DP_RPM_LIST_FILEPATH = System
            .getProperty("user.home") + System.getProperty("file.separator") + ".pkglist"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    public static final String DP_RPM_LIST_MAX_PROPOSALS = "10"; //$NON-NLS-1$
    public static final String DP_RPMTOOLS_RPM = "rpm -qa --qf '%{NAME}\\n'"; //$NON-NLS-1$
    public static final String DP_RPMTOOLS_YUM = "yum -C list all | grep - | cut -d \".\" -f 1"; //$NON-NLS-1$
    public static final String DP_RPMTOOLS_URPM = "urpmq --list"; //$NON-NLS-1$
    public static final boolean DP_RPM_LIST_BACKGROUND_BUILD = true;
    public static final int DP_RPM_LIST_BUILD_PERIOD = 1;

    // macro
    public static final String DP_MACRO_PROPOSALS_FILESPATH = RPMMACRO_FILE
            + ";/usr/lib/rpm/macros;/etc/rpm"; //$NON-NLS-1$
    // changelog
    public static final String DP_CHANGELOG_LOCAL = "US"; //$NON-NLS-1$
    public static final String DP_CHANGELOG_ENTRY_FORMAT = P_CHANGELOG_ENTRY_FORMAT_VERSIONED;
    // task tags
    public static final String DP_TASK_TAGS = "TODO;FIXME"; //$NON-NLS-1$

    // Convert tab by spaces
    public static final boolean DP_SPACES_FOR_TABS = false;
    public static final String DP_NBR_OF_SPACES_FOR_TAB = "4"; //$NON-NLS-1$

    // RPM informations
    public static final boolean DP_RPMINFO_NAME = true;
    public static final boolean DP_RPMINFO_VERSION = true;
    public static final boolean DP_RPMINFO_RELEASE = true;
    public static final boolean DP_RPMINFO_SUMMARY = true;
    public static final boolean DP_RPMINFO_LICENSE = true;
    public static final boolean DP_RPMINFO_GROUP = true;
    public static final boolean DP_RPMINFO_URL = true;
    public static final boolean DP_RPMINFO_DESCRIPTION = false;
    public static final boolean DP_RPMINFO_INSTALLTIME = true;
    public static final boolean DP_RPMINFO_SIZE = true;
    public static final boolean DP_RPMINFO_PACKAGER = false;
    public static final boolean DP_RPMINFO_VENDOR = true;
    public static final boolean DP_RPMINFO_BUILDTIME = true;
    public static final boolean DP_RPMINFO_SOURCERPM = true;

    public static final long DP_RPM_LIST_LAST_BUILD = 0;

    private PreferenceConstants(){
        super();
    }

}
