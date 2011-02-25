/*
 * (c) 2004, 2005 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/

package org.eclipse.linuxtools.rpm.ui;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.linuxtools.rpm.core.IRPMProject;
import org.eclipse.linuxtools.rpm.core.RPMExportDelta;

import org.eclipse.ui.PlatformUI;

import java.lang.reflect.InvocationTargetException;

import java.util.ArrayList;
import java.util.Iterator;

public class RPMExportOperation implements IRunnableWithProgress {
	private IProgressMonitor monitor;
	private ArrayList rpm_errorTable;
	private IRPMProject rpmProject;
	private RPMExportDelta exportDelta;
	private int exportType;
	
	public RPMExportOperation(IRPMProject rpmProject, int exportType,
			RPMExportDelta exportDelta) {
		this.rpmProject = rpmProject;
		this.exportDelta = exportDelta;
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
		rpm_errorTable = new ArrayList();

		// Start progress
		monitor.beginTask(Messages.getString("RPMExportOperation.Starting"), //$NON-NLS-1$
		totalWork);
		monitor.worked(1);
		
		switch(exportType) {
		case IRPMUIConstants.BUILD_ALL:
			try {
				monitor.setTaskName(Messages.getString("RPMExportOperation.Executing_RPM_Export"));
				rpmProject.buildAll(exportDelta);
			} catch(Exception e) {
				rpm_errorTable.add(e);
			}
			break;
		
		case IRPMUIConstants.BUILD_BINARY:
			monitor.setTaskName(Messages.getString("RPMExportOperation.Executing_RPM_Export")); //$NON-NLS-1$
			try {
				rpmProject.buildBinaryRPM(exportDelta);
			} catch(Exception e) {
				rpm_errorTable.add(e);
			}
			break;
		
		case IRPMUIConstants.BUILD_SOURCE:
			monitor.setTaskName(Messages.getString("RPMExportOperation.Executing_SRPM_Export")); //$NON-NLS-1$
			try {
				rpmProject.buildSourceRPM(exportDelta);
			} catch(Exception e) {
				rpm_errorTable.add(e);
			}
			break;
		}
		monitor.worked(1);
	}
	
	public MultiStatus getStatus() {
		IStatus[] errors = new IStatus[rpm_errorTable.size()];
		Iterator count = rpm_errorTable.iterator();
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
					Status.ERROR,
					"RPM Plugin",Status.OK, //$NON-NLS-1$
					error_message,
					null);
			errors[iCount] = error;
			iCount++;
		}

		return new MultiStatus(PlatformUI.PLUGIN_ID, IStatus.OK, errors, Messages.getString("RPMExportOperation.Open_SRPM_Errors"), //$NON-NLS-1$
		null);
	}
	
}
