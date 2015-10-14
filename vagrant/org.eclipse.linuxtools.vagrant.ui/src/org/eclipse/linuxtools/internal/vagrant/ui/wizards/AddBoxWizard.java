/*******************************************************************************
 * Copyright (c) 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.vagrant.ui.wizards;

import org.eclipse.jface.wizard.Wizard;

/**
 * 
 */
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
