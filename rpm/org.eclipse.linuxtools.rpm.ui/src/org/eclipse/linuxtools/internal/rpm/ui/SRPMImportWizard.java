/*******************************************************************************
 * Copyright (c) 2004, 2018 Red Hat, Inc.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.ui;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

/**
 * Plug-in entry point. When the user chooses to import an RPM the plug-in
 * manager in Eclipse will invoke this class. This class extends Wizard and
 * implements IImportWizard. The main plugin class to be used in the desktop.
 * This is the "entrypoint" for the import rpm plug-in.
 */
public class SRPMImportWizard extends Wizard implements IImportWizard {
	private SRPMImportPage mainPage;

	@Override
	public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
		setWindowTitle(Messages.getString("SRPMImportwizard.Import_an_SRPM")); //$NON-NLS-1$
		setNeedsProgressMonitor(true);
	}

	// We have elected to do the Finish button clickin in the SRPMImportPage. So
	// override
	// the default and point to SRPMImport finish()
	@Override
	public boolean performFinish() {
		return mainPage.finish();
	}

	/**
	 * @see org.eclipse.jface.wizard.IWizard#canFinish()
	 *
	 *      Select to finish validation in the SRPMImportPage
	 */
	@Override
	public boolean canFinish() {
		return mainPage.canFinish();
	}

	// Add the SRPMImportPage as the only page in this wizard.
	@Override
	public void addPages() {
		mainPage = new SRPMImportPage();
		addPage(mainPage);
		super.addPages();
	}
}
