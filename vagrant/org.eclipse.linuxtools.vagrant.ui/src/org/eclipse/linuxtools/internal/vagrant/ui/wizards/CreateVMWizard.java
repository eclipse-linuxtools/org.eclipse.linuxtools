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
import org.eclipse.linuxtools.vagrant.core.IVagrantBox;

/**
 * 
 */
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

	public String getBoxName() {
		return this.createVMPage.getBoxName();
	}

	public String getVMFile() {
		return this.createVMPage.getVMFile();
	}

}
