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
package org.eclipse.linuxtools.internal.rpm.createrepo.wizard;

import org.eclipse.linuxtools.internal.rpm.createrepo.Messages;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;

/**
 * The sole purpose of this page is to create a new project for createrepo.
 */
public class CreaterepoNewWizardPageOne extends
		WizardNewProjectCreationPage {

	/*
	 * TODO: add in working sets
	 */

	/**
	 * Constructor for CreaterepoWizardPage. Will set the page name, title, and
	 * description.
	 *
	 * @param pageName The wizard page's name.
	 */
	public CreaterepoNewWizardPageOne(String pageName) {
		super(pageName);
		setTitle(Messages.CreaterepoNewWizardPageOne_wizardPageTitle);
		setDescription(Messages.CreaterepoNewWizardPageOne_wizardPageDescription);
	}

}