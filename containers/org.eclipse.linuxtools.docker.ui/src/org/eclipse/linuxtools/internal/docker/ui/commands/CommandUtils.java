/*******************************************************************************
 * Copyright (c) 2014, 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.docker.ui.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerContainer;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.linuxtools.internal.docker.ui.RunConsole;
import org.eclipse.linuxtools.internal.docker.ui.preferences.PreferenceConstants;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerContainersView;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerExplorerContentProvider.DockerContainersCategory;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerExplorerContentProvider.DockerImagesCategory;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerExplorerView;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerImagesView;
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
	 * @return the current {@link IDockerConnection} associated with the given
	 *         {@link IWorkbenchPart} or {@code null} if none could be found.
	 * @param activePart
	 *            - active Workbench part
	 */
	public static IDockerConnection getCurrentConnection(final IWorkbenchPart activePart) {
		if (activePart instanceof DockerContainersView) {
			return ((DockerContainersView) activePart).getConnection();
		} else if (activePart instanceof DockerImagesView) {
			return ((DockerImagesView) activePart).getConnection();
		} else if (activePart instanceof DockerExplorerView) {
			final ITreeSelection selection = ((DockerExplorerView) activePart)
					.getCommonViewer().getStructuredSelection();
			final Object firstElement = selection.getFirstElement();
			if (firstElement instanceof IDockerConnection) {
				return (IDockerConnection) firstElement;
			} else if (firstElement instanceof IDockerContainer) {
				return ((IDockerContainer) firstElement).getConnection();
			} else if (firstElement instanceof IDockerImage) {
				return ((IDockerImage) firstElement).getConnection();
			} else if (firstElement instanceof DockerContainersCategory) {
				return ((DockerContainersCategory) firstElement)
						.getConnection();
			} else if (firstElement instanceof DockerImagesCategory) {
				return ((DockerImagesCategory) firstElement).getConnection();
			}
		}
		return null;
	}

	/**
	 * @param activePart
	 *            the active {@link IWorkbenchPart}
	 * @return the {@link List} of selected {@link IDockerContainer} in the
	 *         given active part of {@link Collections#emptyList()} if none was
	 *         selected
	 */
	public static List<IDockerContainer> getSelectedContainers(final IWorkbenchPart activePart) {
		if (activePart instanceof DockerContainersView) {
			final ISelection selection = ((DockerContainersView) activePart).getSelection();
			return getSelectedContainers(selection);
		} else if (activePart instanceof DockerExplorerView) {
			final ISelection selection = ((DockerExplorerView) activePart)
					.getCommonViewer().getSelection();
			return getSelectedContainers(selection);
		}
		return Collections.emptyList();
	}

	/**
	 * 
	 * @param selection
	 *            the current selection
	 * @return the {@link List} of {@link IDockerContainer} associated with the
	 *         given {@link ISelection}, or {@link Collections#emptyList()} if
	 *         none was selected.
	 */
	public static List<IDockerContainer> getSelectedContainers(final ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			final List<IDockerContainer> selectedContainers = new ArrayList<>();
			final IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			for (Iterator<?> iterator = structuredSelection.iterator(); iterator.hasNext();) {
				final Object selectedElement = iterator.next();
				if (selectedElement instanceof IDockerContainer) {
					selectedContainers.add((IDockerContainer) selectedElement);
				}
			}
			return Collections.unmodifiableList(selectedContainers);
		}
		return Collections.emptyList();
	}
	/**
	 * @param activePart
	 *            the active {@link IWorkbenchPart}
	 * @return the {@link List} of selected {@link IDockerImage} in the given
	 *         active part of {@link Collections#emptyList()} if none was
	 *         selected
	 */
	public static List<IDockerImage> getSelectedImages(
			final IWorkbenchPart activePart) {
		if (activePart instanceof DockerImagesView) {
			final ISelection selection = ((DockerImagesView) activePart)
					.getSelection();
			return getSelectedImages(selection);
		} else if (activePart instanceof DockerExplorerView) {
			final ISelection selection = ((DockerExplorerView) activePart)
					.getCommonViewer().getSelection();
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
	public static List<IDockerImage> getSelectedImages(
			final ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			final List<IDockerImage> selectedImages = new ArrayList<>();
			final IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			for (Iterator<?> iterator = structuredSelection.iterator(); iterator
					.hasNext();) {
				final Object selectedElement = iterator.next();
				if (selectedElement instanceof IDockerImage) {
					selectedImages.add((IDockerImage) selectedElement);
				}
			}
			return Collections.unmodifiableList(selectedImages);
		}
		return Collections.emptyList();
	}

	/**
	 * Finds and returns a cleared {@link RunConsole} if the preference
	 * {@link PreferenceConstants#AUTOLOG_ON_START} is set to {@code true}.
	 * 
	 * @param connection
	 *            the current Docker connection
	 * @param container
	 *            the container whose log should be sent in the
	 *            {@link RunConsole}.
	 * @return the {@link RunConsole} or {@code null}
	 */
	public static RunConsole getRunConsole(final IDockerConnection connection, final IDockerContainer container) {
		final boolean autoLogOnStart = Activator.getDefault().getPreferenceStore()
				.getBoolean(PreferenceConstants.AUTOLOG_ON_START);
		// if we are auto-logging, grab the
		// console for the container id and get
		// its stream.
		if (autoLogOnStart) {
			final RunConsole console = RunConsole.findConsole(container);
			if (console != null) {
				console.attachToConsole(connection);
				console.clearConsole();
				return console;
			}
		}
		return null;
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

}
