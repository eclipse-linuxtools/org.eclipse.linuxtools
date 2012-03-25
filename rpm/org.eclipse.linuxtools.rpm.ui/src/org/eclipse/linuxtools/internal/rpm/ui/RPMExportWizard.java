/*******************************************************************************
 * Copyright (c) 2004-2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.linuxtools.rpm.core.IRPMConstants;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;

/**
 * Wizard to handle rpm exports.
 *
 */
public class RPMExportWizard extends Wizard implements IExportWizard {
	private RPMExportPage mainPage;
	private IStructuredSelection selection;

	/**
	 * @see org.eclipse.ui.IWorkbenchWizard#init(IWorkbench,
	 *      IStructuredSelection)
	 * 
	 *      Basic constructor. Don't do much, just print out debug, and set
	 *      progress monitor status to true
	 */
	public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
		setNeedsProgressMonitor(true);
		setWindowTitle(Messages.getString("RPMExportWizard.Export_an_SRPM")); //$NON-NLS-1$
		selection = currentSelection;
	}

	@Override
	public boolean performFinish() {
		// Create a new instance of the RPMExportOperation runnable
		final RPMExportOperation rpmExport = new RPMExportOperation(mainPage
				.getSelectedRPMProject(), mainPage.getExportType());
		// Run the export
		rpmExport.setUser(true);
		rpmExport.schedule();
		
		// Need to return some meaningful status. Should only return true if the
		// wizard completed successfully.
		return true;
	}

	@Override
	public boolean canFinish() {
		return mainPage.canFinish();
	}

	// Add the RPMExportPage as the only page in this wizard.
	@Override
	public void addPages() {
		IProject project = null;
		if (selection.isEmpty()) {

		} else if (!(selection.getFirstElement() instanceof IProject)) {
			if (selection.getFirstElement() instanceof IResource) {
				IResource resource = (IResource) selection.getFirstElement();
				IProject parentProject = resource.getProject();
				try {
					if (parentProject.hasNature(IRPMConstants.RPM_NATURE_ID)) {
						project = parentProject;
					}
				} catch (CoreException e) {
					// nothing we can do
				}
			}
		} else if (selection.getFirstElement() instanceof IProject) {
			IProject tempProject = (IProject) selection.getFirstElement();
			try {
				if (tempProject.hasNature(IRPMConstants.RPM_NATURE_ID)) {
					project = tempProject;
				}
			} catch (CoreException e) {
				// nothing we can do
			}
		}
		if (project != null) {
			mainPage = new RPMExportPage(project);
			addPage(mainPage);
		}
	}
}
