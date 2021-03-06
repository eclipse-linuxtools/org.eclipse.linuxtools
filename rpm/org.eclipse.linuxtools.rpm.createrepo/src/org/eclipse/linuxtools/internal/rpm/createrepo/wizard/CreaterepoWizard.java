/*******************************************************************************
 * Copyright (c) 2013, 2018 Red Hat Inc. and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Neil Guzman - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.createrepo.wizard;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.linuxtools.internal.rpm.createrepo.Activator;
import org.eclipse.linuxtools.internal.rpm.createrepo.CreaterepoProjectCreator;
import org.eclipse.linuxtools.internal.rpm.createrepo.ICreaterepoConstants;
import org.eclipse.linuxtools.internal.rpm.createrepo.IRepoFileConstants;
import org.eclipse.linuxtools.internal.rpm.createrepo.Messages;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.ide.IDE;

/**
 * This wizard will guide the user to creating a createrepo project. It will
 * allow the user to initialize their .repo file.
 */
public class CreaterepoWizard extends Wizard implements INewWizard {

    private CreaterepoNewWizardPageOne pageOne;
    private CreaterepoNewWizardPageTwo pageTwo;

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        setNeedsProgressMonitor(true);
    }

    @Override
    public void addPages() {
        super.addPages();
        pageOne = new CreaterepoNewWizardPageOne(
                Messages.CreaterepoNewWizardPageOne_wizardPageName);
        addPage(pageOne);
        pageTwo = new CreaterepoNewWizardPageTwo(
                Messages.CreaterepoNewWizardPageTwo_wizardPageName);
        addPage(pageTwo);
    }

    @Override
    public boolean canFinish() {
        return getContainer().getCurrentPage() == pageTwo && pageTwo.isPageComplete();
    }

    @Override
    public boolean performFinish() {
        WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
            @Override
            protected void execute(IProgressMonitor monitor) {
                createProject(monitor);
            }
        };
        try {
            getContainer().run(false, true, op);
        } catch (InvocationTargetException|InterruptedException e) {
            Activator.logError(Messages.CreaterepoWizard_errorCreatingProject, e);
        }
        return true;
    }

    /**
     * Create a createrepo project that contains an empty content folder and
     * a quickly initialized .repo file with the mandatory options.
     *
     * @param monitor The progress monitor.
     */
    private void createProject(IProgressMonitor monitor) {
        try {
            String fileName = pageTwo.getRepositoryID().concat("."+ICreaterepoConstants. //$NON-NLS-1$
                    REPO_FILE_EXTENSION);
            // create the project
            IProject project = CreaterepoProjectCreator.create(pageOne.getProjectName(),
                    pageOne.getLocationPath(), fileName, monitor);
            // get a handle on the content folder
            IFolder folder = project
                    .getFolder(ICreaterepoConstants.CONTENT_FOLDER);
            if (!folder.exists()) {
                folder.create(false, true, monitor);
            }
            // get a handle on the .repo file
            final IFile file = project.getFile(fileName);
            final String repoFileContents = initializeRepoContents(pageTwo.getRepositoryID(),
                    pageTwo.getRepositoryName(), pageTwo.getRepositoryURL());
            InputStream stream = new ByteArrayInputStream(repoFileContents.getBytes());
            if (file.exists()) {
                file.setContents(stream, true, true, monitor);
            } else {
                file.create(stream, true, monitor);
            }
            monitor.worked(1);
            monitor.setTaskName(Messages.CreaterepoWizard_openFileOnCreation);
            getShell().getDisplay().asyncExec(() -> {
			    IWorkbenchPage page = PlatformUI.getWorkbench()
			            .getActiveWorkbenchWindow().getActivePage();
			    try {
			        IDE.openEditor(page, file, true);
			    } catch (PartInitException e) {
			        Activator.logError(
			                Messages.CreaterepoWizard_errorOpeningNewlyCreatedFile, e);
			    }
			});
            monitor.worked(1);
        } catch (CoreException e) {
            Activator.logError(Messages.CreaterepoWizard_errorCreatingProject, e);
        }
    }

    /**
     * Helper method to initialize the contents of the .repo file.
     *
     * @param id The unique repository ID.
     * @param name A human readable string that describes the repository.
     * @param url A URL pointing to the repodata folder.
     * @return
     */
    private static String initializeRepoContents(String id, String name, String url) {
        String contents = String.format("[%s]\n", id); //$NON-NLS-1$
        contents = contents.concat(String.format("%s=%s\n", IRepoFileConstants.NAME, name)); //$NON-NLS-1$
        return contents.concat(String.format("%s=%s\n", IRepoFileConstants.BASE_URL, url));
    }

}