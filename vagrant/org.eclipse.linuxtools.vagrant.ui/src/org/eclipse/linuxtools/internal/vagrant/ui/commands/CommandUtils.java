/*******************************************************************************
 * Copyright (c) 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.vagrant.ui.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.linuxtools.internal.vagrant.ui.views.VagrantBoxView;
import org.eclipse.linuxtools.internal.vagrant.ui.views.VagrantVMView;
import org.eclipse.linuxtools.vagrant.core.IVagrantBox;
import org.eclipse.linuxtools.vagrant.core.IVagrantVM;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Utility class for all {@link IHandler} command handlers
 * @author xcoulon
 *
 */
public class CommandUtils {

	/**
	 * Refreshes (async) the {@link Viewer}.
	 * 
	 * @param viewer
	 *            - the {@link Viewer} to refresh
	 */
	public static void asyncRefresh(final Viewer viewer) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				if (viewer != null && !viewer.getControl().isDisposed()) {
					viewer.refresh();
				}
			}
		});
	}

	/**
	 * @param activePart
	 *            the active {@link IWorkbenchPart}
	 * @return the {@link List} of selected {@link IVagrantVM} in the
	 *         given active part of {@link Collections#emptyList()} if none was
	 *         selected
	 */
	public static List<IVagrantVM> getSelectedContainers(final IWorkbenchPart activePart) {
		if (activePart instanceof VagrantVMView) {
			final ISelection selection = ((VagrantVMView) activePart).getSelection();
			return getSelectedContainers(selection);
		}
		return Collections.emptyList();
	}

	/**
	 * 
	 * @param selection
	 *            the current selection
	 * @return the {@link List} of {@link IVagrantVM} associated with the
	 *         given {@link ISelection}, or {@link Collections#emptyList()} if
	 *         none was selected.
	 */
	public static List<IVagrantVM> getSelectedContainers(final ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			final List<IVagrantVM> selectedContainers = new ArrayList<>();
			final IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			for (Iterator<?> iterator = structuredSelection.iterator(); iterator.hasNext();) {
				final Object selectedElement = iterator.next();
				if (selectedElement instanceof IVagrantVM) {
					selectedContainers.add((IVagrantVM) selectedElement);
				}
			}
			return Collections.unmodifiableList(selectedContainers);
		}
		return Collections.emptyList();
	}
	/**
	 * @param activePart
	 *            the active {@link IWorkbenchPart}
	 * @return the {@link List} of selected {@link IVagrantBox} in the given
	 *         active part of {@link Collections#emptyList()} if none was
	 *         selected
	 */
	public static List<IVagrantBox> getSelectedImages(
			final IWorkbenchPart activePart) {
		if (activePart instanceof VagrantBoxView) {
			final ISelection selection = ((VagrantBoxView) activePart)
					.getSelection();
			return getSelectedImages(selection);
		}
		return Collections.emptyList();
	}

	/**
	 * 
	 * @param selection
	 *            the current selection
	 * @return the {@link List} of {@link IDockerImage} associated with the
	 *         given {@link ISelection}, or {@link Collections#emptyList()} if
	 *         none was selected.
	 */
	public static List<IVagrantBox> getSelectedImages(
			final ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			final List<IVagrantBox> selectedImages = new ArrayList<>();
			final IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			for (Iterator<?> iterator = structuredSelection.iterator(); iterator
					.hasNext();) {
				final Object selectedElement = iterator.next();
				if (selectedElement instanceof IVagrantBox) {
					selectedImages.add((IVagrantBox) selectedElement);
				}
			}
			return Collections.unmodifiableList(selectedImages);
		}
		return Collections.emptyList();
	}

	/**
	 * Opens the given {@link IWizard} and returns <code>true</code> if the user
	 * finished the operation, <code>false</code> if he cancelled it.
	 * 
	 * @param wizard
	 *            the wizard to open
	 * @param shell
	 *            the current {@link Shell}
	 * @return <code>true</code> if the wizard completed, <code>false</code>
	 *         otherwise.
	 */
	public static boolean openWizard(final IWizard wizard, final Shell shell) {
		final WizardDialog wizardDialog = new WizardDialog(shell, wizard);
		wizardDialog.create();
		return wizardDialog.open() == Window.OK;
	}

	/**
	 * Opens the given {@link IWizard} and returns <code>true</code> if the user
	 * finished the operation, <code>false</code> if he cancelled it.
	 * 
	 * @param wizard
	 *            the wizard to open
	 * @param shell
	 *            the current {@link Shell}
	 * @return <code>true</code> if the wizard completed, <code>false</code>
	 *         otherwise.
	 */
	public static boolean openWizard(final IWizard wizard, final Shell shell,
			final int width, final int height) {
		final WizardDialog wizardDialog = new WizardDialog(shell, wizard);
		wizardDialog.setPageSize(width, height);
		wizardDialog.create();
		return wizardDialog.open() == Window.OK;
	}

	public static void delete(File root) {
		if (root.isDirectory()) {
			for (File child : root.listFiles()) {
				if (child.isDirectory() && child.canRead()) {
					delete(child);
				} else {
					child.delete();
				}
			}
		}
		root.delete();
	}

}
