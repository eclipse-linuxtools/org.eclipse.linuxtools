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

package org.eclipse.linuxtools.rpm.ui.editor.preferences;

/**
 * Constant definitions for plug-in preferences
 */
public class PreferenceConstants {
	/*
	 * Prefences keys
	 */
	
	// rpm list builder
	public static final String P_RPM_LIST_FILEPATH = "rpmListFilePath";	
	public static final String P_RPM_LIST_MAX_PROPOSALS = "rpmListMaxProposalsInfo";
	public static final String P_CURRENT_RPMTOOLS = "currentRpmtools";
	public static final String P_RPM_LIST_BACKGROUND_BUILD = "rpmListBackgroundBuild";
	public static final String P_RPM_LIST_BUILD_PERIOD = "rpmListBuildPeriod";
	public static final String P_RPM_LIST_LAST_BUILD = "rpmLisMastBuild";
	// macro
	public static final String P_MACRO_PROPOSALS_FILESPATH = "macroProposalsFilespath";
	public static final String P_MACRO_HOVER_CONTENT_VIEWCONTENT = "macroHoverViewContent";	
	public static final String P_MACRO_HOVER_CONTENT_VIEWDESCRIPTION = "macroHoverViewDescription";	
	public static final String P_MACRO_HOVER_CONTENT = P_MACRO_HOVER_CONTENT_VIEWCONTENT;
	// changelog
	public static final String P_CHANGELOG_LOCAL = "changelogLocal";
	// RPM informations
	public static final String P_RPMINFO_NAME = "RpmInfoName";
	public static final String P_RPMINFO_VERSION = "RpmInfoVersion";
	public static final String P_RPMINFO_RELEASE = "RpmInfoRelease";
	public static final String P_RPMINFO_SUMMARY = "RpmInfoSummary";
	public static final String P_RPMINFO_LICENSE = "RpmInfoLicense";
	public static final String P_RPMINFO_GROUP = "RpmInfoName";
	public static final String P_RPMINFO_URL = "RpmInfoUrl";
	public static final String P_RPMINFO_DESCRIPTION = "RpmInfoDescription";	
	public static final String P_RPMINFO_INSTALLTIME = "RpmInfoInstallDate";	
	public static final String P_RPMINFO_SIZE = "RpmInfoSize";	
	public static final String P_RPMINFO_PACKAGER = "RpmInfoPackager";	
	public static final String P_RPMINFO_VENDOR = "RpmInfoVendor";
	public static final String P_RPMINFO_BUILDTIME = "RpmInfoBuildDate";
	public static final String P_RPMINFO_SOURCERPM = "RpmInfoSourceRpm";
	
	/*
	 * Preferences default values
	 */
	public static final String DP_RPM_LIST_FILEPATH = System.getProperty("user.dir") +  "/.pkglist";	
	public static final String DP_RPM_LIST_MAX_PROPOSALS = "10";
	public static final String DP_RPMTOOLS_RPM = "rpm -qa --qf '%{NAME}\\n'";
	public static final String DP_RPMTOOLS_YUM = "yum -C list all | grep - | cut -d \".\" -f 1";
	//public static final String DP_RPM_LIST_HIDE_PROPOSALS_WARNING = "false";
	public static final boolean DP_RPM_LIST_BACKGROUND_BUILD = true;
	public static final String DP_RPM_LIST_BUILD_PERIOD = "1";
	//public static final String DP_RPM_LIST_LAST_BUILD = "";
	
	
	// macro
	public static final String DP_MACRO_PROPOSALS_FILESPATH = System.getProperty("user.home") + "/.rpmmacros;/usr/lib/rpm/macros";
	// changelog
	public static final String DP_CHANGELOG_LOCAL = "US";
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
	

}
