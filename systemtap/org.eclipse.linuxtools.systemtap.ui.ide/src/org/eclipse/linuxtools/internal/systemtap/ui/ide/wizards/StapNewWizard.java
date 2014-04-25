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
package org.eclipse.linuxtools.internal.systemtap.ui.ide.wizards;

import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.IDEPerspective;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
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
    private static final ResourceBundle resourceBundle = ResourceBundle.getBundle("org.eclipse.linuxtools.internal.systemtap.ui.ide.wizards.stap_strings"); //$NON-NLS-1$

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

    @Override
    public void addPages() {
        page = new StapNewWizardPage(selection);
        addPage(page);
    }

    /**
     * This method is called when 'Finish' button is pressed in
     * the wizard. We will create an operation and run it
     * using wizard as execution context.
     */
    @Override
    public boolean performFinish() {
        final String containerName = page.getContainerName();
        final String fileName = page.getFileName();
        IRunnableWithProgress op = new IRunnableWithProgress() {
            @Override
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
            MessageDialog.openError(getShell(), "Error", e.getLocalizedMessage()); //$NON-NLS-1$
            return false;
        } catch (InvocationTargetException e) {
            Throwable realException = e.getTargetException();
            MessageDialog.openError(getShell(), "Error", realException.getMessage()); //$NON-NLS-1$
            return false;
        }
        return true;
    }

    /**
     * The worker method. It will find the container, create the
     * file if missing or just replace its contents, and open
     * the editor on the newly created file.
     */

    private void doFinish(String containerName,    String fileName, IProgressMonitor monitor) throws CoreException {
        // create a .stp file

        monitor.beginTask(resourceBundle.getString("StapNewWizard.BeginTask") + fileName, 2); //$NON-NLS-1$
        final IContainer newResource = (IContainer) ResourcesPlugin.getWorkspace().getRoot().findMember(containerName);
        final IFile newFile = newResource.getFile(new Path(fileName));
        String envString = "#!/usr/bin/env stap"; //$NON-NLS-1$
        newFile.create(new ByteArrayInputStream(envString.getBytes()) , true, monitor);
        monitor.worked(1);
        monitor.setTaskName(resourceBundle.getString("StapNewWizard.SetTask")); //$NON-NLS-1$
        getShell().getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
                try {
                    IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getWorkbench()
                            .showPerspective(IDEPerspective.ID, PlatformUI.getWorkbench().getActiveWorkbenchWindow());
                    IDE.openEditor(page, newFile);
                } catch (WorkbenchException e1) {
                    // ignore, the file is created but opening the editor failed
                }
            }
        });
        monitor.worked(1);
    }

    /**
     * We will accept the selection in the workbench to see if
     * we can initialize from it.
     * @see INewWizard#init(IWorkbench, IStructuredSelection)
     */
    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.selection = selection;
    }
}
