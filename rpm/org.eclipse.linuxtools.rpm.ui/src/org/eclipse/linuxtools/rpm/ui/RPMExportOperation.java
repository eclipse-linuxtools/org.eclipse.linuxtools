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
import org.eclipse.linuxtools.rpm.core.RPMProject;
import org.eclipse.linuxtools.rpm.ui.IRPMUIConstants.BuildType;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

public class RPMExportOperation implements IRunnableWithProgress {
	private IProgressMonitor monitor;
	private ArrayList<Exception> rpm_errorTable;
	private RPMProject rpmProject;
	private BuildType exportType;

	public RPMExportOperation(RPMProject rpmProject, BuildType exportType) {
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
		MessageConsole myConsole = findConsole("rpmbuild"); //$NON-NLS-1$
		MessageConsoleStream out = myConsole.newMessageStream();
		myConsole.clearConsole();
		myConsole.activate();
		switch (exportType) {
		case ALL:
			try {
				monitor.setTaskName(Messages
						.getString("RPMExportOperation.Executing_RPM_Export")); //$NON-NLS-1$
				rpmProject.buildAll(out);
			} catch (Exception e) {
				rpm_errorTable.add(e);
			}
			break;

		case BINARY:
			monitor.setTaskName(Messages
					.getString("RPMExportOperation.Executing_RPM_Export")); //$NON-NLS-1$
			try {
				rpmProject.buildBinaryRPM(out);
			} catch (Exception e) {
				rpm_errorTable.add(e);
			}
			break;

		case SOURCE:
			monitor.setTaskName(Messages
					.getString("RPMExportOperation.Executing_SRPM_Export")); //$NON-NLS-1$
			try {
				rpmProject.buildSourceRPM(out);
			} catch (Exception e) {
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
		String error_message = Messages.getString("RPMExportOperation.0"); //$NON-NLS-1$
		while (count.hasNext()) {

			Object anonErrorObject = count.next();
			if (anonErrorObject instanceof Throwable) {
				Throwable errorObject = (Throwable) anonErrorObject;
				error_message = errorObject.getMessage();

			} else if (anonErrorObject instanceof Status) {
				Status errorObject = (Status) anonErrorObject;
				error_message = errorObject.getMessage();
			}
			IStatus error = new Status(IStatus.ERROR, "RPM Plugin", IStatus.OK, //$NON-NLS-1$
					error_message, null);
			errors[iCount] = error;
			iCount++;
		}

		return new MultiStatus(PlatformUI.PLUGIN_ID, IStatus.OK, errors,
				Messages.getString("RPMExportOperation.Open_SRPM_Errors"), //$NON-NLS-1$
				null);
	}

	private MessageConsole findConsole(String name) {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		for (int i = 0; i < existing.length; i++)
			if (name.equals(existing[i].getName()))
				return (MessageConsole) existing[i];
		// no console found, so create a new one
		MessageConsole myConsole = new MessageConsole(name, null);
		conMan.addConsoles(new IConsole[] { myConsole });
		return myConsole;
	}

}
