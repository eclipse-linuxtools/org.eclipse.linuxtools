/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
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

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.linuxtools.rpm.core.RPMProjectNature;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;

public class RPMNewProject extends Wizard implements INewWizard {
	WizardNewProjectCreationPage namePage;

	public RPMNewProject() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean performFinish() {
		try {
			WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
				protected void execute(IProgressMonitor monitor) {
					createProject(monitor != null ? monitor
							: new NullProgressMonitor());
				}
			};
			getContainer().run(false, true, op);
		} catch (InvocationTargetException x) {
			return false;
		} catch (InterruptedException x) {
			return false;
		}
		return true;

	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		super.addPages();
		namePage = new WizardNewProjectCreationPage("New RPM Project");
		namePage.setTitle("Create a new RPM project");
		namePage
				.setDescription("Created project will have a standard rpmbuild structure.");
		namePage.setImageDescriptor(ImageDescriptor.createFromFile(getClass(),
				"/icons/rpm.gif"));
		addPage(namePage);
	}

	protected void createProject(IProgressMonitor monitor) {
		try {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IProject project = root.getProject(namePage.getProjectName());
			IProjectDescription description = ResourcesPlugin.getWorkspace()
					.newProjectDescription(project.getName());
			if (!Platform.getLocation().equals(namePage.getLocationPath()))
				description.setLocation(namePage.getLocationPath());
			description
					.setNatureIds(new String[] { RPMProjectNature.RPM_NATURE_ID });
			project.create(description, monitor);
			monitor.worked(10);
			project.open(monitor);
			project.getFolder("SPECS").create(true, true, monitor);
			project.getFolder("SOURCES").create(true, true, monitor);
			IFolder buildFolder = project.getFolder("BUILD");
			buildFolder.create(true, true, monitor);
			buildFolder.setHidden(true);
			project.getFolder("RPMS").create(true, true, monitor);
			project.getFolder("SRPMS").create(true, true, monitor);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

}
