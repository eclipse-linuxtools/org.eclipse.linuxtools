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
package org.eclipse.linuxtools.rpm.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.linuxtools.rpm.core.IRPMProject;
import org.eclipse.ui.PlatformUI;

public class RPMExportOperation implements IRunnableWithProgress {
	private IProgressMonitor monitor;
	private ArrayList<Exception> rpm_errorTable;
	private IRPMProject rpmProject;
	private int exportType;
	
	public RPMExportOperation(IRPMProject rpmProject, int exportType) {
		this.rpmProject = rpmProject;
		this.exportType = exportType;
	}

	/**
	 * @see org.eclipse.jface.operation.IRunnableWithProgress#run(IProgressMonitor)
	 *
	 */
	public void run(IProgressMonitor progressMonitor)
		throws InvocationTargetException {
		int totalWork = 2;

		monitor = progressMonitor;

		// We keep a all our reported errors in an ArrayList.
		rpm_errorTable = new ArrayList<Exception>();

		// Start progress
		monitor.beginTask(Messages.getString("RPMExportOperation.Starting"), //$NON-NLS-1$
		totalWork);
		monitor.worked(1);
		
		switch(exportType) {
		case IRPMUIConstants.BUILD_ALL:
			try {
				monitor.setTaskName(Messages.getString("RPMExportOperation.Executing_RPM_Export")); //$NON-NLS-1$
				rpmProject.buildAll();
			} catch(Exception e) {
				rpm_errorTable.add(e);
			}
			break;
		
		case IRPMUIConstants.BUILD_BINARY:
			monitor.setTaskName(Messages.getString("RPMExportOperation.Executing_RPM_Export")); //$NON-NLS-1$
			try {
				rpmProject.buildBinaryRPM();
			} catch(Exception e) {
				rpm_errorTable.add(e);
			}
			break;
		
		case IRPMUIConstants.BUILD_SOURCE:
			monitor.setTaskName(Messages.getString("RPMExportOperation.Executing_SRPM_Export")); //$NON-NLS-1$
			try {
				rpmProject.buildSourceRPM();
			} catch(Exception e) {
				rpm_errorTable.add(e);
			}
			break;
		}
		monitor.worked(1);
	}
	
	public MultiStatus getStatus() {
		IStatus[] errors = new IStatus[rpm_errorTable.size()];
		Iterator<Exception> count = rpm_errorTable.iterator();
		int iCount = 0;
		String error_message=Messages.getString("RPMExportOperation.0"); //$NON-NLS-1$
		while (count.hasNext()) {

			Object anonErrorObject = count.next();
			if (anonErrorObject instanceof Throwable) {
				Throwable errorObject = (Throwable)  anonErrorObject;
				error_message=errorObject.getMessage();
				
			}
			else
				if (anonErrorObject instanceof Status)
				{
					Status errorObject = (Status) anonErrorObject;
					error_message=errorObject.getMessage();
				}
			IStatus error =
				new Status(
					IStatus.ERROR,
					"RPM Plugin",IStatus.OK, //$NON-NLS-1$
					error_message,
					null);
			errors[iCount] = error;
			iCount++;
		}

		return new MultiStatus(PlatformUI.PLUGIN_ID, IStatus.OK, errors, Messages.getString("RPMExportOperation.Open_SRPM_Errors"), //$NON-NLS-1$
		null);
	}
	
}
