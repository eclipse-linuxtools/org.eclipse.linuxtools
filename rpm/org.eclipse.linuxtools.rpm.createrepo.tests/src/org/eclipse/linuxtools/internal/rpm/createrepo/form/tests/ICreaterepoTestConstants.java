/*******************************************************************************
 * Copyright (c) 2013, 2020 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Neil Guzman - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.createrepo.form.tests;

import org.eclipse.linuxtools.internal.rpm.createrepo.CreaterepoPreferenceConstants;


/**
 * Constants used for testing.
 */
public interface ICreaterepoTestConstants {

    /*
     * Dialog specific stuff
     */
    String MAIN_SHELL = "Resource - Eclipse Platform"; //$NON-NLS-1$
    String FILE = "File"; //$NON-NLS-1$
    String NEW = "New"; //$NON-NLS-1$
    String OTHER = "Other..."; //$NON-NLS-1$
    String NEXT_BUTTON = "Next >"; //$NON-NLS-1$
    String BACK_BUTTON = "< Back"; //$NON-NLS-1$
    String FINISH_BUTTON = "Finish"; //$NON-NLS-1$
    String CANCEL_BUTTON = "Cancel"; //$NON-NLS-1$
    String WINDOW = "Window"; //$NON-NLS-1$
    String SHOW_VIEW = "Show View"; //$NON-NLS-1$
    String PREFERENCES = "Preferences"; //$NON-NLS-1$
    String APPLY_AND_CLOSE_BUTTON = "Apply and Close"; //$NON-NLS-1$
    String OK_BUTTON = "OK"; //$NON-NLS-1$
    String OPEN = "Open"; //$NON-NLS-1$
    String DEFAULTS = "Restore Defaults"; //$NON-NLS-1$
    String PROPERTIES = "Properties"; //$NON-NLS-1$
    String PROPERTIES_SHELL = "Properties for %s"; //$NON-NLS-1$
    String SELECT_A_WIZARD = "Select a wizard"; //$NON-NLS-1$

    /*
     * Navigator controls
     */
    String GO_INTO = "Go Into"; //$NON-NLS-1$
    String GO_BACK = "Back to Workspace"; //$NON-NLS-1$
    String GO_FORWARD = "Forward"; //$NON-NLS-1$

    /*
     * Views
     */
    String WELCOME_VIEW = "Welcome"; //$NON-NLS-1$
    String GENERAL_NODE = "General"; //$NON-NLS-1$
    String PROJECT_EXPLORER = "Project Explorer"; //$NON-NLS-1$

    /*
     * Project Wizard Specific Stuff
     */
    String PROJECT_NAME_LABEL = "Project name:"; //$NON-NLS-1$
    String CREATEREPO_CATEGORY = "Createrepo"; //$NON-NLS-1$
    String CREATEREPO_PROJECT_CATEGORY = "RPM"; //$NON-NLS-1$
    String CREATEREPO_PROJECT_WIZARD = "Createrepo Wizard"; //$NON-NLS-1$

    /*
     * Resources
     */
    String RPM_RESOURCE_LOC =  "resources" + System.getProperty("file.separator")  //$NON-NLS-1$//$NON-NLS-2$
            + "rpms" + System.getProperty("file.separator"); //$NON-NLS-1$ //$NON-NLS-2$
    String RPM1 = "eclipse-egit-github-3.0.0-2.fc19.noarch.rpm"; //$NON-NLS-1$
    String RPM2 = "hello-2.8-1.fc19.src.rpm"; //$NON-NLS-1$

    /*
     * Common createrepo files
     */
    String REPODATA_FOLDER = "repodata"; //$NON-NLS-1$
    String REPO_MD_NAME = "repomd.xml"; //$NON-NLS-1$

    /*
     * Test names
     */
    String PROJECT_NAME = "createrepo-test-project"; //$NON-NLS-1$
    String REPO_NAME = "createrepo-test-repo.repo"; //$NON-NLS-1$

    /*
     * Preferences used
     */
    String DELTAS = "Deltas"; //$NON-NLS-1$
    String[] PREFS_ARRAY = {
        CreaterepoPreferenceConstants.PREF_DISTRO_TAG,
        CreaterepoPreferenceConstants.PREF_CONTENT_TAG,
        CreaterepoPreferenceConstants.PREF_REPO_TAG,
        CreaterepoPreferenceConstants.PREF_REVISION,
        CreaterepoPreferenceConstants.PREF_UNIQUE_MD_NAME,
        CreaterepoPreferenceConstants.PREF_GENERATE_DB,
        CreaterepoPreferenceConstants.PREF_IGNORE_SYMLINKS,
        CreaterepoPreferenceConstants.PREF_PRETTY_XML,
        CreaterepoPreferenceConstants.PREF_WORKERS,
        CreaterepoPreferenceConstants.PREF_CHECK_TS,
        CreaterepoPreferenceConstants.PREF_CHANGELOG_LIMIT,
        CreaterepoPreferenceConstants.PREF_CHECKSUM,
        CreaterepoPreferenceConstants.PREF_COMPRESSION_TYPE,
        CreaterepoPreferenceConstants.PREF_GENERAL_ENABLED,
        CreaterepoPreferenceConstants.PREF_DELTA_ENABLE,
        CreaterepoPreferenceConstants.PREF_NUM_DELTAS,
        CreaterepoPreferenceConstants.PREF_MAX_DELTA_SIZE,
        CreaterepoPreferenceConstants.PREF_OLD_PACKAGE_DIRS,
    };
}
