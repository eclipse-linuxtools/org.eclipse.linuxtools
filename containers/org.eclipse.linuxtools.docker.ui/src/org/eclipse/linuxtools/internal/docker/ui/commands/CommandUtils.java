/*******************************************************************************
 * Copyright (c) 2014, 2023 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.docker.ui.commands;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerContainer;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.docker.core.IDockerPortMapping;
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.linuxtools.internal.docker.ui.DockerConnectionWatcher;
import org.eclipse.linuxtools.internal.docker.ui.consoles.RunConsole;
import org.eclipse.linuxtools.internal.docker.ui.preferences.PreferenceConstants;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerContainersView;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerExplorerContentProvider.DockerContainerVolume;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerExplorerView;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerImageHierarchyView;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerImagesView;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;

/**
 * Utility class for all {@link IHandler} command handlers
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
		Display.getDefault().asyncExec(() -> {
			if (viewer != null && !viewer.getControl().isDisposed()) {
				viewer.refresh();
			}
		});
	}

	/**
	 * @return the current {@link IDockerConnection} associated with the given
	 *         {@link IWorkbenchPart} or {@code null} if none could be found.
	 * @param activePart
	 *            - active Workbench part
	 */
	public static IDockerConnection getCurrentConnection(
			final IWorkbenchPart activePart) {
		if (DockerConnectionWatcher.getInstance().getConnection() != null)
			return DockerConnectionWatcher.getInstance().getConnection();
		else if (activePart instanceof DockerContainersView) {
			return ((DockerContainersView) activePart).getConnection();
		} else if (activePart instanceof DockerImagesView) {
			return ((DockerImagesView) activePart).getConnection();
		}
		// fall back to first active connection in list if one exists
		return Stream.of(DockerConnectionManager.getInstance().getConnections())
				.filter(c -> c.isOpen()).findFirst().orElse(null);
	}

	/**
	 * @param activePart
	 *            the active {@link IWorkbenchPart}
	 * @return the {@link List} of selected elements in the given active part or
	 *         {@link Collections#emptyList()} if none was selected
	 */
	public static List<Object> getSelectedElements(
			final IWorkbenchPart activePart) {
		return getSelectedElements(activePart, Object.class);
	}

	/**
	 * @param activePart
	 *            the active {@link IWorkbenchPart}
	 * @return the of selected element in the given active part or
	 *         <code>null</code> if none was found
	 */
	public static Object getSelectedElement(final IWorkbenchPart activePart) {
		final List<Object> selectedElements = getSelectedElements(activePart,
				Object.class);
		if (selectedElements != null && !selectedElements.isEmpty()) {
			return selectedElements.get(0);
		}
		return null;
	}

	/**
	 * @param activePart
	 *            the active {@link IWorkbenchPart}
	 * @param targetClass
	 * @return the {@link List} of selected {@link IDockerContainer} in the
	 *         given active part of {@link Collections#emptyList()} if none was
	 *         selected
	 */
	private static <T> List<T> getSelectedElements(
			final IWorkbenchPart activePart, final Class<T> targetClass) {
		if (activePart instanceof DockerContainersView) {
			final IStructuredSelection selection = ((DockerContainersView) activePart)
					.getStructuredSelection();
			return castSelectionTo(selection, targetClass);
		} else if (activePart instanceof DockerImagesView) {
			final IStructuredSelection selection = ((DockerImagesView) activePart)
					.getStructuredSelection();
			return castSelectionTo(selection, targetClass);
		} else if (activePart instanceof DockerExplorerView) {
			final IStructuredSelection selection = ((DockerExplorerView) activePart)
					.getCommonViewer().getStructuredSelection();
			return castSelectionTo(selection, targetClass);
		} else if (activePart instanceof DockerImageHierarchyView) {
			final IStructuredSelection selection = ((DockerImageHierarchyView) activePart)
					.getCommonViewer().getStructuredSelection();
			return adaptSelectionTo(selection, targetClass);
		}
		return Collections.emptyList();
	}

	/**
	 * @param activePart
	 *            the active {@link IWorkbenchPart}
	 * @return the {@link List} of selected {@link IDockerContainer} in the
	 *         given active part of {@link Collections#emptyList()} if none was
	 *         selected
	 */
	public static List<IDockerContainer> getSelectedContainers(
			final IWorkbenchPart activePart) {
		return getSelectedElements(activePart, IDockerContainer.class);
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
		return getSelectedElements(activePart, IDockerPortMapping.class);
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
		return getSelectedElements(activePart, DockerContainerVolume.class);
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
		return getSelectedElements(activePart, IDockerImage.class);
	}

	/**
	 * @param activePart
	 *            the active {@link IWorkbenchPart}
	 * @return the selected {@link IDockerImage} in the given active part of
	 *         <code>null</code> if none was selected
	 */
	public static IDockerImage getSelectedImage(
			final IWorkbenchPart activePart) {
		final List<IDockerImage> selectedImages = getSelectedImages(activePart);
		if (selectedImages == null || selectedImages.isEmpty()) {
			return null;
		}
		return selectedImages.get(0);
	}

	/**
	 * Returns the given {@link List} <strong>typed</strong> with the given
	 * {@code targetClass}.
	 *
	 * @param selection
	 *            the current selection
	 * @return Returns the given {@link List} typed with the
	 *         {@code targetClass}.
	 */
	@SuppressWarnings("unchecked")
	private static <T> List<T> castSelectionTo(
			final IStructuredSelection selection, final Class<T> targetClass) {
		return selection.toList().stream()
				.filter(selectedElement -> targetClass
						.isAssignableFrom(selectedElement.getClass()))
				.map(selectedElement -> (T) selectedElement)
				.toList();
	}

	/**
	 * Returns the given {@link List} <strong>adapted</strong> to the given
	 * {@code targetClass}.
	 *
	 * @param selection
	 *            the current selection
	 * @param targetClass
	 *            the target class in which the elements of the given
	 *            {@code selection} should be adapted
	 * @return Returns the given {@link List} adapted to the
	 *         {@code targetClass}.
	 */
	@SuppressWarnings("unchecked")
	private static <T> List<T> adaptSelectionTo(
			final IStructuredSelection selection, final Class<T> targetClass) {
		return selection.toList().stream()
				.filter(selectedElement -> selectedElement instanceof IAdaptable adaptable
						&& adaptable.getAdapter(targetClass) != null)
				.map(selectedElement -> ((IAdaptable) selectedElement).getAdapter(targetClass)).toList();
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
			RunConsole.attachToTerminal(connection, container.id(), null);
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
	 * @param width
	 *            width of the wizard
	 * @param height
	 *            height of the wizard
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

	/**
	 * Executes the command identified by the given {@code id} on a context
	 * based on the given selection
	 *
	 * @param id
	 *            the id of the command to execute
	 * @param selection
	 *            the selection to use as a context to run the command
	 */
	public static void execute(final String id,
			final IStructuredSelection selection) {
		final ICommandService service = PlatformUI.getWorkbench()
				.getService(ICommandService.class);
		final Command command = service != null ? service.getCommand(id) : null;
		if (command != null && command.isDefined()) {
			try {
				ParameterizedCommand pCmd = ParameterizedCommand.generateCommand(command, null);
				IHandlerService handlerSvc = PlatformUI.getWorkbench().getService(IHandlerService.class);
				IEvaluationContext ctx = handlerSvc.getCurrentState();
				ctx = new EvaluationContext(ctx, selection);
				ctx.addVariable(ISources.ACTIVE_CURRENT_SELECTION_NAME, selection);
				handlerSvc.executeCommandInContext(pCmd, null, ctx);
			} catch (Exception e) {
				Activator.log(e);
			}
		}
	}

}
