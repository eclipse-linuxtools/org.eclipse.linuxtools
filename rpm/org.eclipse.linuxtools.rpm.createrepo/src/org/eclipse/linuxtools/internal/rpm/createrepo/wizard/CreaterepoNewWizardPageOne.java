/*******************************************************************************
 * Copyright (c) 2013, 2018 Red Hat Inc. and others.
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