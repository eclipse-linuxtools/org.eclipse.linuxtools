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
public class PackageVMWizard extends Wizard {

	private final PackageVMPage packageVMPage;

	public PackageVMWizard() {
		super();
		setWindowTitle(WizardMessages.getString("PackageVMWizard.title")); //$NON-NLS-1$
		this.packageVMPage = new PackageVMPage();
	}

	@Override
	public void addPages() {
		addPage(packageVMPage);
	}

	@Override
	public boolean canFinish() {
		return this.packageVMPage.isPageComplete();
	}

	@Override
	public boolean performFinish() {
		return true;
	}

	public String getBoxName() {
		return this.packageVMPage.getBoxName();
	}

	public String getBoxFolder() {
		return this.packageVMPage.getBoxFolder();
	}

}
