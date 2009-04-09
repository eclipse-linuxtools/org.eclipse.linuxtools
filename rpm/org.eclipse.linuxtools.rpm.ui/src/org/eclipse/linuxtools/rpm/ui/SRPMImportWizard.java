/*
 * (c) 2004, 2005 Red Hat, Inc.
 *
 * This program is open source software licensed under the 
 * Eclipse Public License ver. 1
*/

/**
 * @author pmuldoon
 * @version 1.0
 *
 *
 * Plug-in entry point. When the user chooses to import an RPM the plug-in manager in Eclipse
 * will invoke this class. This class extends Wizard and implements IImportWizard.
 */
package org.eclipse.linuxtools.rpm.ui;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;

import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;


/**
 * The main plugin class to be used in the desktop. This is the "entrypoint"
 * for the import rpm plug-in.
 */
public class SRPMImportWizard extends Wizard implements IImportWizard {
	private IWorkbench workbench;
	private IStructuredSelection selection;
	private SRPMImportPage mainPage;

	/**
	 * @see org.eclipse.ui.IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
		this.workbench = workbench;
		selection = currentSelection;
		setWindowTitle(Messages.getString("SRPMImportwizard.Import_an_SRPM")); //$NON-NLS-1$
		setNeedsProgressMonitor(true);
	}

	/**
	 * @see org.eclipse.jface.wizard.IWizard#performFinish()
	 */

	// We have elected to do the Finish button clickin in the SRPMImportPage. So override
	//the default and point to SRPMImport finish()
	public boolean performFinish() {
		try {
			return mainPage.finish();
		} catch (CoreException e) {
			return false;
		}
	}

	/**
	 * @see org.eclipse.jface.wizard.IWizard#canFinish()
	 *
	 * Select to finish validation in the SRPMImportPage
	 */
	public boolean canFinish() {
		return mainPage.canFinish();
	}

	/**
	 * @see org.eclipse.jface.wizard.IWizard#addPages()
	 */

	// Add the SRPMImportPage as the only page in this wizard.
	public void addPages() {
		mainPage = new SRPMImportPage(workbench, selection);
		addPage(mainPage);
		super.addPages();
	}
}
