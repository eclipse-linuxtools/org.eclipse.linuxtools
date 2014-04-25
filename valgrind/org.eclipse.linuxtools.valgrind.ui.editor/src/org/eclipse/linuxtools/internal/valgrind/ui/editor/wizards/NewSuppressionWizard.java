/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.ui.editor.wizards;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.linuxtools.internal.valgrind.ui.editor.ValgrindEditorPlugin;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.plugin.AbstractUIPlugin;

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

public class NewSuppressionWizard extends Wizard implements INewWizard {
    private static final String EXT_SUPP = "supp"; //$NON-NLS-1$
    private WizardNewFileCreationPage page;
    private IStructuredSelection selection;

    /**
     * Constructor for NewSuppressionWizard.
     */
    public NewSuppressionWizard() {
        super();
        setNeedsProgressMonitor(true);
    }

    /**
     * Adding the page to the wizard.
     */

    @Override
    public void addPages() {
        page = new WizardNewFileCreationPage("newSuppressionPage", selection); //$NON-NLS-1$
        page.setTitle(Messages.getString("NewSuppressionWizard.NewWizard_title")); //$NON-NLS-1$
        page.setDescription(Messages.getString("NewSuppressionWizard.NewWizard_description")); //$NON-NLS-1$
        page.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(ValgrindEditorPlugin.PLUGIN_ID, "icons/newsupp_wiz.png")); //$NON-NLS-1$
        page.setFileExtension(EXT_SUPP);
        addPage(page);
    }

    /**
     * This method is called when 'Finish' button is pressed in
     * the wizard. We will create an operation and run it
     * using wizard as execution context.
     */
    @Override
    public boolean performFinish() {
        final String containerName = page.getContainerFullPath().toOSString();
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
            return false;
        } catch (InvocationTargetException e) {
            Throwable realException = e.getTargetException();
            MessageDialog.openError(getShell(), Messages.getString("NewSuppressionWizard.performFinish_error"), realException.getMessage()); //$NON-NLS-1$
            return false;
        }
        return true;
    }

    /**
     * The worker method. It will find the container, create the
     * file if missing or just replace its contents, and open
     * the editor on the newly created file.
     */

    private void doFinish(
        String containerName,
        String fileName,
        IProgressMonitor monitor)
        throws CoreException {
        // create a sample file
        monitor.beginTask(NLS.bind(Messages.getString("NewSuppressionWizard.Creating_task"), fileName), 2); //$NON-NLS-1$
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IResource resource = root.findMember(new Path(containerName));
        if (!resource.exists() || !(resource instanceof IContainer)) {
            throwCoreException(NLS.bind(Messages.getString("NewSuppressionWizard.Container_not_exist_error"), containerName)); //$NON-NLS-1$
        }
        IContainer container = (IContainer) resource;
        final IFile file = container.getFile(new Path(fileName));
        try (InputStream stream = openContentStream()){
            if (file.exists()) {
                file.setContents(stream, true, true, monitor);
            } else {
                file.create(stream, true, monitor);
            }
        } catch (IOException e) {
        }
        monitor.worked(1);
        monitor.setTaskName(Messages.getString("NewSuppressionWizard.Opening_file_task")); //$NON-NLS-1$
        getShell().getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
                IWorkbenchPage page =
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                try {
                    IDE.openEditor(page, file, true);
                } catch (PartInitException e) {
                }
            }
        });
        monitor.worked(1);
    }

    /**
     * We will initialize file contents with a sample text.
     */

    private InputStream openContentStream() {
        return new ByteArrayInputStream("{\n\t\n}".getBytes());
    }

    private void throwCoreException(String message) throws CoreException {
        IStatus status =
            new Status(IStatus.ERROR, ValgrindEditorPlugin.PLUGIN_ID, IStatus.OK, message, null);
        throw new CoreException(status);
    }

    /**
     * We will accept the selection in the workbench to see if
     * we can initialize from it.
     * @see org.eclipse.ui.IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
     */
    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.selection = selection;
    }
}