/*******************************************************************************
 * Copyright (c) 2015, 2018 Red Hat.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.vagrant.ui.wizards;

import org.eclipse.jface.wizard.Wizard;

public class AddBoxWizard extends Wizard {

	private final AddBoxPage imagePullPage;

	public AddBoxWizard() {
		super();
		setWindowTitle(WizardMessages.getString("ImagePull.title")); //$NON-NLS-1$
		this.imagePullPage = new AddBoxPage();
	}

	@Override
	public void addPages() {
		addPage(imagePullPage);
	}

	@Override
	public boolean canFinish() {
		return this.imagePullPage.isPageComplete();
	}

	@Override
	public boolean performFinish() {
		return true;
	}

	public String getBoxName() {
		return this.imagePullPage.getBoxName();
	}

	public String getBoxLoc() {
		return this.imagePullPage.getBoxLoc();
	}

}
