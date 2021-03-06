/*******************************************************************************
 * Copyright (c) 2009, 2018 Red Hat, Inc.
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
package org.eclipse.linuxtools.internal.rpm.ui;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.linuxtools.rpm.core.RPMProjectCreator;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

/**
 * Use RPMProjectCreator for the actual work.
 *
 */
public class RPMNewProject extends Wizard implements INewWizard {
	private NewProjectCreationPage namePage;

	@Override
	public boolean performFinish() {
		try {
			WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
				@Override
				protected void execute(IProgressMonitor monitor) throws CoreException {
					createProject(monitor != null ? monitor : new NullProgressMonitor());
				}
			};
			getContainer().run(false, true, op);
		} catch (InvocationTargetException | InterruptedException x) {
			return false;
		}
		return true;
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		super.addPages();
		namePage = new NewProjectCreationPage(Messages.getString("RPMNewProject.0")); //$NON-NLS-1$
		namePage.setTitle(Messages.getString("RPMNewProject.1")); //$NON-NLS-1$
		namePage.setDescription(Messages.getString("RPMNewProject.2")); //$NON-NLS-1$
		namePage.setImageDescriptor(ImageDescriptor.createFromFile(getClass(), "/icons/rpm.gif")); //$NON-NLS-1$
		addPage(namePage);
		namePage.init(selection);
	}

	protected void createProject(IProgressMonitor monitor) throws CoreException {
		RPMProjectCreator rpmProjectCreator = new RPMProjectCreator(namePage.getSelectedLayout());
		IProject project = rpmProjectCreator.create(namePage.getProjectName(), namePage.getLocationPath(), monitor);
		// Add new project to working sets, if requested
		IWorkingSet[] workingSets = namePage.getWorkingSets();
		if (workingSets.length > 0) {
			PlatformUI.getWorkbench().getWorkingSetManager().addToWorkingSets(project, workingSets);
		}
	}
}
