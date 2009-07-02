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
package org.eclipse.linuxtools.rpm.ui;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.progress.IProgressConstants;

public class RPMExportWizard extends Wizard implements IExportWizard {
	private RPMExportPage mainPage;
	private IStructuredSelection selection;
	
	/**
	 * @see org.eclipse.ui.IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 *
	 * Basic constructor. Don't do much, just print out debug, and set progress
	 * monitor status to true
	 */
	public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
		setNeedsProgressMonitor(true);
		setWindowTitle(Messages.getString("RPMExportWizard.Export_an_SRPM")); //$NON-NLS-1$
		selection = currentSelection;
	}

	@Override
	public boolean performFinish() {
		// Create a new instance of the RPMExportOperation runnable
		final RPMExportOperation rpmExport = new RPMExportOperation(mainPage.getSelectedRPMProject(),
				mainPage.getExportType()); 
		
		// Run the export
		Job job = new Job(Messages.getString("RPMExportWizard.0")) { //$NON-NLS-1$
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				setProperty(IProgressConstants.KEEP_PROPERTY, Boolean.TRUE);
				try {
					rpmExport.run(monitor);
				} catch (InvocationTargetException e) {
					return Status.CANCEL_STATUS;
				}
				return rpmExport.getStatus();
			}
		};
		job.setUser(true);
		job.schedule();
				
		// Need to return some meaninful status. Should only return true if the wizard completed
		// successfully.
		return true;
	}

	@Override
	public boolean canFinish() {
		return mainPage.canFinish();
	}

	// Add the RPMExportPage as the only page in this wizard.
	@Override
	public void addPages() {
		mainPage = new RPMExportPage(selection);
		addPage(mainPage);
	}
}
