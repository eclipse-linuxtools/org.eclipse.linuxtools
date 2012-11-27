/*******************************************************************************
 * Copyright (c) 2012 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - Initial Wizard and related API
 *******************************************************************************/
package org.eclipse.linuxtools.systemtap.ui.ide.wizards;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ResourceBundle;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.linuxtools.systemtap.ui.ide.IDEPerspective;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.ide.IDE;

/**
 * This is a sample new wizard. Its role is to create a new file
 * resource in the provided container. If the container resource
 * (a folder or a project) is selected in the workspace
 * when the wizard is opened, it will accept it as the target
 * container. The wizard creates one file with the extension
 * "mpe". If a sample multi-page editor (also available
 * as a template) is registered for the same extension, it will
 * be able to open it.
 */

public class StapNewWizard extends Wizard implements INewWizard {
	private StapNewWizardPage page;
	private ISelection selection;
	private static final ResourceBundle resourceBundle = ResourceBundle.getBundle("org.eclipse.linuxtools.systemtap.ui.ide.wizards.stap_strings"); //$NON-NLS-1$

	/**
	 * Constructor for StapNewWizard.
	 */
	public StapNewWizard() {
		super();
		setNeedsProgressMonitor(true);
	}

	/**
	 * Adding the page to the wizard.
	 */

	public void addPages() {
		page = new StapNewWizardPage(selection);
		addPage(page);
	}

	/**
	 * This method is called when 'Finish' button is pressed in
	 * the wizard. We will create an operation and run it
	 * using wizard as execution context.
	 */
	public boolean performFinish() {
		final String containerName = page.getContainerName();
		final String fileName = page.getFileName();
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					doFinish(containerName, fileName, monitor);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		};
		try {
			getContainer().run(true, false, op);
		} catch (InterruptedException e) {
			MessageDialog.openError(getShell(), "Error", e.getLocalizedMessage());
			return false;
		} catch (InvocationTargetException e) {
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), "Error", realException.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * The worker method. It will find the container, create the
	 * file if missing or just replace its contents, and open
	 * the editor on the newly created file.
	 */

	private void doFinish(String containerName,	String fileName, IProgressMonitor monitor) throws CoreException {
		// create a .stp file

		monitor.beginTask(resourceBundle.getString("StapNewWizard.BeginTask") + fileName, 2); //$NON-NLS-1$
		final File newFile = new File(containerName, fileName);
		try {
			String envString = "#!/usr/bin/env stap"; //$NON-NLS-1$
			FileOutputStream FOS = new FileOutputStream(newFile);
			newFile.createNewFile();
			FOS.write(envString.getBytes());
			FOS.close();
		} catch (IOException e) {
			throwCoreException("Error: " + e);
		}
		monitor.worked(1);
		monitor.setTaskName(resourceBundle.getString("StapNewWizard.SetTask")); //$NON-NLS-1$
		getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				try {
					IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getWorkbench()
							.showPerspective(IDEPerspective.ID, PlatformUI.getWorkbench().getActiveWorkbenchWindow());
					IDE.openEditorOnFileStore(page, EFS.getLocalFileSystem().fromLocalFile(newFile));
				} catch (WorkbenchException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		monitor.worked(1);
	}

	private void throwCoreException(String message) throws CoreException {
		IStatus status =
			new Status(IStatus.ERROR, "org.eclipse.linuxtools.systemtap.ui.ide", IStatus.OK, message, null); //$NON-NLS-1$
		throw new CoreException(status);
	}

	/**
	 * We will accept the selection in the workbench to see if
	 * we can initialize from it.
	 * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}
}