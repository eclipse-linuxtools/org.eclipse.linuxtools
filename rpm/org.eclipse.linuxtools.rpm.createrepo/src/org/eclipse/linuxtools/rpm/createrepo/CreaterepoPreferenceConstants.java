/*******************************************************************************
 * Copyright (c) 2013 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Neil Guzman - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.createrepo;

/**
 * Constants used for createrepo preferences.
 */
public final class CreaterepoPreferenceConstants {

	/*
	 * Preference Keys.
	 */
	/****/ public static final String PREF_DISTRO_TAG 	= "distro"; 	//$NON-NLS-1$
	/****/ public static final String PREF_CONTENT_TAG 	= "content"; 	//$NON-NLS-1$
	/****/ public static final String PREF_REPO_TAG 	= "repo"; 		//$NON-NLS-1$
	/****/ public static final String PREF_REVISION 	= "revision"; 	//$NON-NLS-1$

	// CreaterepoPreferencePage
	/****/ public static final String PREF_UNIQUE_MD_NAME		= "md-filenames"; 	//$NON-NLS-1$
	/****/ public static final String PREF_GENERATE_DB			= "database"; 		//$NON-NLS-1$
	/****/ public static final String PREF_IGNORE_SYMLINKS		= "skip-symlinks"; 	//$NON-NLS-1$
	/****/ public static final String PREF_PRETTY_XML			= "pretty"; 		//$NON-NLS-1$
	/****/ public static final String PREF_WORKERS				= "workers"; 		//$NON-NLS-1$
	/****/ public static final String PREF_CHECK_TS				= "checkts"; 		//$NON-NLS-1$
	/****/ public static final String PREF_CHANGELOG_LIMIT		= "changelog-limit"; //$NON-NLS-1$
	/****/ public static final String PREF_CHECKSUM				= "checksum"; 		//$NON-NLS-1$
	/****/ public static final String PREF_COMPRESSION_TYPE		= "compress-type"; 	//$NON-NLS-1$

	// CreaterepoDeltaPreferencePage
	/****/ public static final String PREF_DELTA_ENABLE		= "deltas"; 	//$NON-NLS-1$
	/****/ public static final String PREF_NUM_DELTAS		= "num-deltas"; //$NON-NLS-1$
	/****/ public static final String PREF_MAX_DELTA_SIZE	= "max-delta-rpm-size"; //$NON-NLS-1$

	/*
	 * Preference Values.
	 */
	// CreaterepoPreferencePage
	/****/ public static final boolean DEFAULT_UNIQUE_MD_NAME 		= true;
	/****/ public static final boolean DEFAULT_GENERATE_DB 			= true;
	/****/ public static final boolean DEFAULT_IGNORE_SYMLINKS 		= false;
	/****/ public static final boolean DEFAULT_PRETTY 				= false;
	// createrepo will automatically set workers if 0
	/****/ public static final int DEFAULT_NUM_WORKERS 				= 0;
	/****/ public static final boolean DEFAULT_CHECK_TS 			= false;
	// createrepo will automatically set limit to whatever is in rpm if 0
	/****/ public static final int DEFAULT_CHANGELOG_LIMIT 			= 0;
	/****/ public static final String DEFAULT_CHECKSUM 				= ICreaterepoChecksums.SHA256;
	/****/ public static final String DEFAULT_COMPRESS_TYPE 		= ICreaterepoCompressionTypes.COMPAT;

	// CreaterepoDeltaPreferencePage
	/****/ public static final boolean DEFAULT_DELTA_ENABLE 		= false;
	/****/ public static final int DEFAULT_NUM_DELTAS 				= 1;
	/****/ public static final int DEFAULT_MAX_DELTA_SIZE 			= 10;
}
