/*******************************************************************************
 * Copyright (c) 2004, 2022 Red Hat, Inc.
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
package org.eclipse.linuxtools.rpm.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.linuxtools.internal.rpm.ui.BuildType;
import org.eclipse.linuxtools.internal.rpm.ui.Messages;
import org.eclipse.linuxtools.internal.rpm.ui.RpmConsole;
import org.eclipse.linuxtools.rpm.core.RPMProject;
import org.eclipse.ui.console.IOConsoleOutputStream;

/**
 * Job for handling rpm exports.
 * 
 * @since 1.1
 */
public class RPMExportOperation extends Job {
	private RPMProject rpmProject;
	private BuildType exportType;

	/**
	 * Creates the job for exporting rpms.
	 *
	 * @param rpmProject The project to use as base for the export operation.
	 * @param exportType The export type. [SOURCE, BINARY, ALL]
	 */
	public RPMExportOperation(RPMProject rpmProject, String exportType) {
		super(Messages.getString("RPMExportWizard.0")); //$NON-NLS-1$
		this.rpmProject = rpmProject;
		this.exportType = BuildType.valueOf(exportType);
	}

	@Override
	public IStatus run(IProgressMonitor monitor) {
		IStatus result = null;
		if (rpmProject.getSpecFile() == null) {
			return Status.error(Messages.getString("RPMExportOperation.No_Spec_File")); //$NON-NLS-1$
		}
		IOConsoleOutputStream out = RpmConsole.findConsole(rpmProject).linkJob(this);
		if (out == null) {
			return Status.CANCEL_STATUS;
		}
		switch (exportType) {
		case ALL:
			try {
				monitor.beginTask(Messages.getString("RPMExportOperation.Executing_All_Export"), //$NON-NLS-1$
						IProgressMonitor.UNKNOWN);
				result = rpmProject.buildAll(out);
			} catch (CoreException e) {
				result = Status.error(e.getMessage(), e);
			}
			break;

		case BINARY:
			monitor.beginTask(Messages.getString("RPMExportOperation.Executing_RPM_Export"), IProgressMonitor.UNKNOWN); //$NON-NLS-1$
			try {
				result = rpmProject.buildBinaryRPM(out);
			} catch (CoreException e) {
				result = Status.error(e.getMessage(), e);
			}
			break;

		case SOURCE:
			monitor.beginTask(Messages.getString("RPMExportOperation.Executing_SRPM_Export"), IProgressMonitor.UNKNOWN); //$NON-NLS-1$
			try {
				result = rpmProject.buildSourceRPM(out);
			} catch (CoreException e) {
				result = Status.error(e.getMessage(), e);
			}
			break;
		}
		return result;
	}

	/**
	 * Cancel the operation by repeatedly interrupting the working thread until it
	 * terminates.
	 */
	@Override
	protected void canceling() {
		Thread pollThread = new Thread(() -> {
			while (getResult() == null) {
				Thread thread = getThread();
				if (thread != null) {
					thread.interrupt();
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					break;
				}
			}
		});
		pollThread.start();
	}
}
