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
package org.eclipse.linuxtools.internal.rpm.rpmlint.actions;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.linuxtools.internal.rpm.rpmlint.Activator;
import org.eclipse.linuxtools.internal.rpm.rpmlint.builder.RpmlintNature;
import org.eclipse.linuxtools.internal.rpm.rpmlint.preferences.PreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

/**
 * Toggle rpmlint nature for the given project. As a result this
 * enables/disables rpmlint builder.
 *
 */
public class ToggleRpmlintNatureAction extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) {
		IStructuredSelection selection = HandlerUtil.getCurrentStructuredSelection(event);
		for (Object element : selection.toList()) {
			IProject project = null;
			if (element instanceof IProject p) {
				project = p;
			} else if (element instanceof IAdaptable a) {
				project = a.getAdapter(IProject.class);
			}
			if (project != null) {
				toggleNature(project);
			}
		}
		return null;
	}

	/**
	 * Toggles rpmlint nature on a project.
	 *
	 * @param project The project on which to toggle the nature.
	 */
	private static void toggleNature(IProject project) {
		String rpmlintPath = new ScopedPreferenceStore(InstanceScope.INSTANCE, Activator.PLUGIN_ID)
				.getString(PreferenceConstants.P_RPMLINT_PATH);
		if (!Files.exists(Paths.get(rpmlintPath))) {
			IStatus warning = Status.warning(Messages.RunRpmlintAction_1);
			ErrorDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
					Messages.RunRpmlintAction_2, null, warning);
			return;
		}
		try {
			IProjectDescription description = project.getDescription();
			String[] natures = description.getNatureIds();

			for (int i = 0; i < natures.length; ++i) {
				if (RpmlintNature.NATURE_ID.equals(natures[i])) {
					// Remove the nature
					String[] newNatures = new String[natures.length - 1];
					System.arraycopy(natures, 0, newNatures, 0, i);
					System.arraycopy(natures, i + 1, newNatures, i, natures.length - i - 1);
					description.setNatureIds(newNatures);
					project.setDescription(description, null);
					return;
				}
			}

			// Add the nature
			String[] newNatures = new String[natures.length + 1];
			System.arraycopy(natures, 0, newNatures, 0, natures.length);
			newNatures[natures.length] = RpmlintNature.NATURE_ID;
			description.setNatureIds(newNatures);
			project.setDescription(description, null);
		} catch (CoreException e) {
			// TODO log exception
		}
	}

}
