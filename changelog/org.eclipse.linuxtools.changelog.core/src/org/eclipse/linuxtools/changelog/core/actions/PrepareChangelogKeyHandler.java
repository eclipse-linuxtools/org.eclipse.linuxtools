/*******************************************************************************
 * Copyright (c) 2006 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kyu Lee <klee@redhat.com> - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.changelog.core.actions;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.linuxtools.changelog.core.ChangelogPlugin;
import org.eclipse.ui.IContributorResourceAdapter;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.ResourceSelectionDialog;
import org.eclipse.ui.ide.IContributorResourceAdapter2;

/**
 * 
 * @author klee
 *
 */
public class PrepareChangelogKeyHandler implements IHandler {
	private ResourceMapping getResourceMapping(Object o) {
		if (o instanceof ResourceMapping) {
			return (ResourceMapping) o;
		}
		if (o instanceof IAdaptable) {
			IAdaptable adaptable = (IAdaptable) o;
			Object adapted = adaptable.getAdapter(ResourceMapping.class);
			if (adapted instanceof ResourceMapping) {
				return (ResourceMapping) adapted;
			}
			adapted = adaptable.getAdapter(IContributorResourceAdapter.class);
			if (adapted instanceof IContributorResourceAdapter2) {
				IContributorResourceAdapter2 cra = (IContributorResourceAdapter2) adapted;
				return cra.getAdaptedResourceMapping(adaptable);
			}
		} else {
			Object adapted = Platform.getAdapterManager().getAdapter(o,
					ResourceMapping.class);
			if (adapted instanceof ResourceMapping) {
				return (ResourceMapping) adapted;
			}
		}
		return null;
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {

		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IProject[] currentProject;

		
		// try getting currently selected project
		try {
			IEditorPart editor = getWorkbench().getActiveWorkbenchWindow()
					.getActivePage().getActiveEditor();
			currentProject = (getResourceMapping(editor
					.getEditorInput()).getProjects());

		} catch (Exception e) {
			// if fail, no default selection
			currentProject = new IProject[] {};
		}
		

	
		
	
		ResourceSelectionDialog rsd = new ResourceSelectionDialog(
				ChangelogPlugin.getDefault().getWorkbench()
						.getActiveWorkbenchWindow().getShell(), workspaceRoot,
				"Choose resources to be included in preparing changelog.");

		rsd.setInitialSelections(currentProject);

		rsd.open();

		final Object[] result = rsd.getResult();
		
	
		if (result == null)
			return null; // user didn't select anything or pressed cancel
		
		
		

		try {
		
		Action exampleAction;
		exampleAction = new PrepareChangeLogAction() {
			public void run() {
				setSelection(new StructuredSelection(result));

				doRun();
			}
		};

		exampleAction.run();
		
		} catch (Exception e) {
			ChangelogPlugin.getDefault().getLog().log(
					new Status(IStatus.ERROR, "Changelog", IStatus.ERROR,
							e.getMessage(), e));
		}



		return null;
	}

	protected IWorkbench getWorkbench() {
		return ChangelogPlugin.getDefault().getWorkbench();
	}

	public void addHandlerListener(IHandlerListener handlerListener) {

	}

	public void dispose() {

	}

	public boolean isEnabled() {

		return true;
	}

	public boolean isHandled() {

		return true;
	}

	public void removeHandlerListener(IHandlerListener handlerListener) {

	}

}
