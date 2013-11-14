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

import org.eclipse.osgi.util.NLS;

/**
 * Messages displayed across the plugin.
 */
public final class Messages {

	private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.rpm.createrepo.messages"; //$NON-NLS-1$

	// CreaterepoWizard
	/****/
	public static String CreaterepoWizard_errorCreatingProject;
	/****/
	public static String CreaterepoWizard_openFileOnCreation;
	/****/
	public static String CreaterepoWizard_errorOpeningNewlyCreatedFile;

	// CreaterepoNewWizardPageOne
	/****/
	public static String CreaterepoNewWizardPageOne_wizardPageName;
	/****/
	public static String CreaterepoNewWizardPageOne_wizardPageTitle;
	/****/
	public static String CreaterepoNewWizardPageOne_wizardPageDescription;

	// CreaterepoNewWizardPageTwo
	/****/
	public static String CreaterepoNewWizardPageTwo_wizardPageName;
	/****/
	public static String CreaterepoNewWizardPageTwo_wizardPageTitle;
	/****/
	public static String CreaterepoNewWizardPageTwo_wizardPageDescription;
	/****/
	public static String CreaterepoNewWizardPageTwo_labelID;
	/****/
	public static String CreaterepoNewWizardPageTwo_labelName;
	/****/
	public static String CreaterepoNewWizardPageTwo_labelURL;
	/****/
	public static String CreaterepoNewWizardPageTwo_errorID;
	/****/
	public static String CreaterepoNewWizardPageTwo_errorName;
	/****/
	public static String CreaterepoNewWizardPageTwo_errorURL;

	// RepoFormEditor
	/****/
	public static String RepoFormEditor_errorInitializingForm;
	/****/
	public static String RepoFormEditor_errorInitializingProject;

	// ImportRPMsPage
	/****/
	public static String ImportRPMsPage_title;
	/****/
	public static String ImportRPMsPage_formHeaderText;
	/****/
	public static String ImportRPMsPage_sectionTitle;
	/****/
	public static String ImportRPMsPage_sectionInstruction;
	/****/
	public static String ImportRPMsPage_buttonImportRPMs;
	/****/
	public static String ImportRPMsPage_buttonRemoveRPMs;
	/****/
	public static String ImportRPMsPage_buttonDeleteRPMs;
	/****/
	public static String ImportRPMsPage_buttonCreateRepo;
	/****/
	public static String ImportRPMsPage_errorRefreshingTree;

	// MainOverviewPage
	/****/
	public static String MainOverviewPage_title;
	/****/
	public static String MainOverviewPage_formHeaderText;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

}
