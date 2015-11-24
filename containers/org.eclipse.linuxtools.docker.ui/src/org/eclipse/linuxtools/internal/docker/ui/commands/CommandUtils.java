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
import org.eclipse.linuxtools.docker.core.IDockerPortMapping;
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.linuxtools.internal.docker.ui.RunConsole;
import org.eclipse.linuxtools.internal.docker.ui.preferences.PreferenceConstants;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerContainersView;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerExplorerContentProvider.DockerContainerLink;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerExplorerContentProvider.DockerContainerLinksCategory;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerExplorerContentProvider.DockerContainerPortMappingsCategory;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerExplorerContentProvider.DockerContainerVolume;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerExplorerContentProvider.DockerContainerVolumesCategory;
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
			} else if (firstElement instanceof DockerContainersCategory) {
				return ((DockerContainersCategory) firstElement)
						.getConnection();
			} else if (firstElement instanceof DockerContainerLinksCategory) {
				return ((DockerContainerLinksCategory) firstElement)
						.getContainer().getConnection();
			} else if (firstElement instanceof DockerContainerLink) {
				return ((DockerContainerLink) firstElement).getContainer()
						.getConnection();
			} else
				if (firstElement instanceof DockerContainerPortMappingsCategory) {
				return ((DockerContainerPortMappingsCategory) firstElement)
						.getContainer().getConnection();
			} else if (firstElement instanceof IDockerPortMapping) {
				return ((IDockerPortMapping) firstElement)
						.getContainer()
						.getConnection();
			} else if (firstElement instanceof DockerContainerVolumesCategory) {
				return ((DockerContainerVolumesCategory) firstElement)
						.getContainer().getConnection();
			} else if (firstElement instanceof DockerContainerVolume) {
				return ((DockerContainerVolume) firstElement).getContainer()
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
			return convertSelectionTo(selection, IDockerContainer.class);
		} else if (activePart instanceof DockerExplorerView) {
			final ISelection selection = ((DockerExplorerView) activePart)
					.getCommonViewer().getSelection();
			return convertSelectionTo(selection, IDockerContainer.class);
		}
		return Collections.emptyList();
	}

	/**
	 * @param activePart
	 *            the active {@link IWorkbenchPart}
	 * @return the {@link List} of selected {@link DockerContainerPortMapping}
	 *         in the given active part of {@link Collections#emptyList()} if
	 *         none was selected
	 */
	public static List<IDockerPortMapping> getSelectedPortMappings(
			final IWorkbenchPart activePart) {
		if (activePart instanceof DockerContainersView) {
			final ISelection selection = ((DockerContainersView) activePart)
					.getSelection();
			return convertSelectionTo(selection, IDockerPortMapping.class);
		} else if (activePart instanceof DockerExplorerView) {
			final ISelection selection = ((DockerExplorerView) activePart)
					.getCommonViewer().getSelection();
			return convertSelectionTo(selection, IDockerPortMapping.class);
		}
		return Collections.emptyList();
	}

	/**
	 * @param activePart
	 *            the active {@link IWorkbenchPart}
	 * @return the {@link List} of selected {@link DockerContainerVolume} in the
	 *         given active part of {@link Collections#emptyList()} if none was
	 *         selected
	 */
	public static List<DockerContainerVolume> getSelectedVolumes(
			final IWorkbenchPart activePart) {
		if (activePart instanceof DockerContainersView) {
			final ISelection selection = ((DockerContainersView) activePart)
					.getSelection();
			return convertSelectionTo(selection, DockerContainerVolume.class);
		} else if (activePart instanceof DockerExplorerView) {
			final ISelection selection = ((DockerExplorerView) activePart)
					.getCommonViewer().getSelection();
			return convertSelectionTo(selection, DockerContainerVolume.class);
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
			return convertSelectionTo(selection, IDockerImage.class);
		} else if (activePart instanceof DockerExplorerView) {
			final ISelection selection = ((DockerExplorerView) activePart)
					.getCommonViewer().getSelection();
			return convertSelectionTo(selection, IDockerImage.class);
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
	@SuppressWarnings("unchecked")
	private static <T> List<T> convertSelectionTo(final ISelection selection,
			final Class<T> targetClass) {
		if (selection instanceof IStructuredSelection) {
			final List<T> selectedContainers = new ArrayList<>();
			final IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			for (Iterator<?> iterator = structuredSelection.iterator(); iterator
					.hasNext();) {
				final Object selectedElement = iterator.next();
				if (targetClass.isAssignableFrom(selectedElement.getClass())) {
					selectedContainers.add((T) selectedElement);
				}
			}
			return Collections.unmodifiableList(selectedContainers);
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
		if (container != null
				&& connection.getContainerInfo(container.id()) != null
				&& connection.getContainerInfo(container.id()).config() != null
				&& connection.getContainerInfo(container.id()).config().tty()) {
			RunConsole.attachToTerminal(connection, container.id());
			return null;
		}
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
