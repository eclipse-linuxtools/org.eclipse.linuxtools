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

import java.util.Map;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.linuxtools.vagrant.core.IVagrantBox;

public class CreateVMWizard extends Wizard {

	private final CreateVMPage createVMPage;

	public CreateVMWizard(IVagrantBox box) {
		super();
		setWindowTitle("Create VM"); //$NON-NLS-1$
		this.createVMPage = new CreateVMPage(box);
	}

	@Override
	public void addPages() {
		addPage(createVMPage);
	}

	@Override
	public boolean canFinish() {
		return this.createVMPage.isPageComplete();
	}

	@Override
	public boolean performFinish() {
		return true;
	}

	public String getVMName() {
		return this.createVMPage.getVMName();
	}

	public String getBoxReference() {
		return this.createVMPage.getBoxReference();
	}

	public String getVMFile() {
		return this.createVMPage.getVMFile();
	}

	public Map<String, String> getVMEnvironment() {
		return this.createVMPage.getEnvironment();
	}

}
