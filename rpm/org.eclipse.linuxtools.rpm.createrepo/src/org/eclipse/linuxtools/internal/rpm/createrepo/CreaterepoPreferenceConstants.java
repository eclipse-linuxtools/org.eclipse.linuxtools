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
package org.eclipse.linuxtools.internal.rpm.createrepo;

/**
 * Constants used for createrepo preferences, as well as the command arguments
 * of the execution.
 */
public interface CreaterepoPreferenceConstants {

    /*
     * Preference Keys.
     */
    String PREF_DISTRO_TAG = "distro"; //$NON-NLS-1$
    String PREF_CONTENT_TAG = "content"; //$NON-NLS-1$
    String PREF_REPO_TAG = "repo"; //$NON-NLS-1$
    String PREF_REVISION = "revision"; //$NON-NLS-1$

    // CreaterepoPreferencePage
    String PREF_UNIQUE_MD_NAME = "md-filenames"; //$NON-NLS-1$
    String PREF_GENERATE_DB = "database"; //$NON-NLS-1$
    String PREF_IGNORE_SYMLINKS = "skip-symlinks"; //$NON-NLS-1$
    String PREF_PRETTY_XML = "pretty"; //$NON-NLS-1$
    String PREF_WORKERS = "workers"; //$NON-NLS-1$
    String PREF_CHECK_TS = "checkts"; //$NON-NLS-1$
    String PREF_CHANGELOG_LIMIT = "changelog-limit"; //$NON-NLS-1$
    String PREF_CHECKSUM = "checksum"; //$NON-NLS-1$
    String PREF_COMPRESSION_TYPE = "compress-type"; //$NON-NLS-1$

    // CreaterepoGeneralPropertyPage
    String PREF_GENERAL_ENABLED = "projectSettings"; //$NON-NLS-1$

    // CreaterepoDeltaPropertyPage
    String PREF_DELTA_ENABLE = "deltas"; //$NON-NLS-1$
    String PREF_NUM_DELTAS = "num-deltas"; //$NON-NLS-1$
    String PREF_MAX_DELTA_SIZE = "max-delta-rpm-size"; //$NON-NLS-1$
    String PREF_OLD_PACKAGE_DIRS = "oldpackagedirs"; //$NON-NLS-1$

    // Defaults
    String PREF_VERBOSE = "verbose"; //$NON-NLS-1$
    String PREF_PROFILE = "profile"; //$NON-NLS-1$

    // Misc
    String PREF_UPDATE = "update"; //$NON-NLS-1$

    /*
     * Preference Values.
     */
    // CreaterepoPreferencePage
    boolean DEFAULT_UNIQUE_MD_NAME = true;
    boolean DEFAULT_GENERATE_DB = true;
    boolean DEFAULT_IGNORE_SYMLINKS = false;
    boolean DEFAULT_PRETTY = false;
    // createrepo will automatically set workers if 0
    int DEFAULT_NUM_WORKERS = 0;
    boolean DEFAULT_CHECK_TS = false;
    // createrepo will automatically set limit to whatever is in rpm if 0
    int DEFAULT_CHANGELOG_LIMIT = 0;
    String DEFAULT_CHECKSUM = ICreaterepoChecksums.SHA256;
    String DEFAULT_COMPRESS_TYPE = ICreaterepoCompressionTypes.COMPAT;

    // CreaterepoGeneralPropertyPage
    boolean DEFAULT_GENERAL_ENABLED = false;

    // CreaterepoDeltaPropertyPage
    boolean DEFAULT_DELTA_ENABLE = false;
    int DEFAULT_NUM_DELTAS = 1;
    int DEFAULT_MAX_DELTA_SIZE = 10;
    String DEFAULT_OLD_PACKAGE_DIR_LIST = ICreaterepoConstants.EMPTY_STRING;
}
