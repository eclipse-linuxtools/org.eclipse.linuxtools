/*******************************************************************************
 * Copyright (c) 2004 - 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.linuxtools.rpm.core.RPMProject;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleOutputStream;
import org.osgi.framework.FrameworkUtil;

/**
 * Job for handling rpm exports.
 * 
 */
public class RPMExportOperation extends Job {
	private IProgressMonitor monitor;
	private RPMProject rpmProject;
	private BuildType exportType;

	/**
	 * Creates the job for exporting rpms.
	 * 
	 * @param rpmProject The project to use as base for the export operation.
	 * @param exportType The export type.
	 */
	public RPMExportOperation(RPMProject rpmProject, BuildType exportType) {
		super(Messages.getString("RPMExportWizard.0")); //$NON-NLS-1$
		this.rpmProject = rpmProject;
		this.exportType = exportType;
	}

	/**
	 * @see org.eclipse.jface.operation.IRunnableWithProgress#run(IProgressMonitor)
	 * 
	 */
	@Override
	public IStatus run(IProgressMonitor progressMonitor) {
		int totalWork = 2;

		monitor = progressMonitor;

		// Start progress
		monitor.beginTask(Messages.getString("RPMExportOperation.Starting"), //$NON-NLS-1$
				totalWork);
		monitor.worked(1);
		BuildThread bt = new BuildThread(exportType, rpmProject);
		bt.start();
		while (bt.getState() != Thread.State.TERMINATED) {
			if (monitor.isCanceled()) {
				bt.interrupt();
				return Status.CANCEL_STATUS;
			}
		}
		monitor.worked(1);
		monitor.done();
		if (bt.getResult() != null){
			return bt.getResult();
		}
		return Status.OK_STATUS;
	}

	private IOConsole findConsole() {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		for (int i = 0; i < existing.length; i++)
			if (RpmConsole.ID.equals(existing[i].getName()))
				return (RpmConsole) existing[i];
		// no console found, so create a new one
		RpmConsole myConsole = new RpmConsole(rpmProject);
		conMan.addConsoles(new IConsole[] { myConsole });
		return myConsole;
	}

	private class BuildThread extends Thread {
		private BuildType exportType;
		private RPMProject rpmProject;
		private IStatus result = null;

		public BuildThread(BuildType exportType, RPMProject rpmProject) {
			this.exportType = exportType;
			this.rpmProject = rpmProject;
		}

		@Override
		public void run() {
			IOConsole myConsole = findConsole();
			IOConsoleOutputStream out = myConsole.newOutputStream();
			myConsole.clearConsole();
			myConsole.activate();
			switch (exportType) {
			case ALL:
				try {
					monitor.setTaskName(Messages
							.getString("RPMExportOperation.Executing_RPM_Export")); //$NON-NLS-1$
					result = rpmProject.buildAll(out);
				} catch (CoreException e) {
					result = new Status(IStatus.ERROR, FrameworkUtil.getBundle(this.getClass()).getSymbolicName(),
							e.getMessage(), e);
				}
				break;

			case BINARY:
				monitor.setTaskName(Messages
						.getString("RPMExportOperation.Executing_RPM_Export")); //$NON-NLS-1$
				try {
					result = rpmProject.buildBinaryRPM(out);
				} catch (CoreException e) {
					result = new Status(IStatus.ERROR, FrameworkUtil.getBundle(this.getClass()).getSymbolicName(),
							e.getMessage(), e);
				}
				break;

			case SOURCE:
				monitor.setTaskName(Messages
						.getString("RPMExportOperation.Executing_SRPM_Export")); //$NON-NLS-1$
				try {
					result = rpmProject.buildSourceRPM(out);
				} catch (CoreException e) {
					result = new Status(IStatus.ERROR, FrameworkUtil.getBundle(this.getClass()).getSymbolicName(),
							e.getMessage(), e);
				}
				break;
			}
			
		}
		
		public IStatus getResult(){
			return result;
		}
	}
}
