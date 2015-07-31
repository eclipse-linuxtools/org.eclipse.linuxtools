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

import static org.eclipse.linuxtools.internal.docker.ui.commands.CommandUtils.getRunConsole;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerContainer;
import org.eclipse.linuxtools.docker.core.IDockerContainerConfig;
import org.eclipse.linuxtools.docker.core.IDockerHostConfig;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.linuxtools.internal.docker.ui.RunConsole;
import org.eclipse.linuxtools.internal.docker.ui.views.DVMessages;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerExplorerView;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerImagesView;
import org.eclipse.linuxtools.internal.docker.ui.wizards.ImageRun;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * @author xcoulon
 *
 */
public class RunImageCommandHandler extends AbstractHandler {

	private static final String ERROR_CREATING_CONTAINER = "ContainerCreateError.msg"; //$NON-NLS-1$

	@Override
	public Object execute(final ExecutionEvent event) {
		final IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
		final IDockerImage selectedImage = getSelectedImage(activePart);
		if (selectedImage == null) {
			Activator.logErrorMessage(
					DVMessages.getString("RunImageUnableToRetrieveError.msg")); //$NON-NLS-1$
		} else {
			try {
				final ImageRun wizard = new ImageRun(selectedImage);
				final boolean runImage = CommandUtils.openWizard(wizard,
						HandlerUtil.getActiveShell(event));
				if (runImage) {
					final IDockerContainerConfig containerConfig = wizard
							.getDockerContainerConfig();
					final IDockerHostConfig hostConfig = wizard
							.getDockerHostConfig();
					runImage(selectedImage.getConnection(), containerConfig,
							hostConfig, wizard.getDockerContainerName());
				}
			} catch (DockerException e) {
				Activator.log(e);
			}
		}
		return null;
	}

	private void runImage(final IDockerConnection connection,
			final IDockerContainerConfig containerConfig,
			final IDockerHostConfig hostConfig, final String containerName) {
		if (containerConfig.tty()) {
			// show the console view
			try {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow()
						.getActivePage()
						.showView(IConsoleConstants.ID_CONSOLE_VIEW);
			} catch (PartInitException e) {
				Activator.log(e);
			}
		}

		// Create the container in a non-UI thread.
		final Job runImageJob = new Job(
				DVMessages.getString("RunImageCreateContainer.job")) { //$NON-NLS-1$

			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				monitor.beginTask(
						DVMessages.getString("RunImageRunningTask.msg"), 2); //$NON-NLS-1$
				try {
					final SubProgressMonitor createContainerMonitor = new SubProgressMonitor(
							monitor, 1);
					// create the container
					createContainerMonitor.beginTask(
							DVMessages.getString(
									"RunImageCreatingContainerTask.msg"), //$NON-NLS-1$
							1);
					final String containerId = ((DockerConnection) connection)
							.createContainer(containerConfig, hostConfig, containerName);
					final IDockerContainer container = ((DockerConnection) connection)
							.getContainer(containerId);

					createContainerMonitor.done();
					// abort if operation was cancelled
					if (monitor.isCanceled()) {
						return Status.CANCEL_STATUS;
					}
					// start the container
					final SubProgressMonitor startContainerMonitor = new SubProgressMonitor(
							monitor, 1);
					startContainerMonitor.beginTask(DVMessages
							.getString("RunImageStartingContainerTask.msg"), 1); //$NON-NLS-1$
					final RunConsole console = getRunConsole(connection,
							container);
					if (console != null) {
						// if we are auto-logging, show the console
						console.showConsole();
					}
					((DockerConnection) connection).startContainer(containerId,
							console.getOutputStream());
					startContainerMonitor.done();
				} catch (final DockerException | InterruptedException e) {
					Display.getDefault().syncExec(new Runnable() {

						@Override
						public void run() {
							MessageDialog.openError(
									Display.getCurrent().getActiveShell(),
									DVMessages.getFormattedString(
											ERROR_CREATING_CONTAINER,
											containerConfig.image()),
									e.getMessage());

						}

					});
				} finally {
					monitor.done();
				}
				return Status.OK_STATUS;
			}
		};
		runImageJob.schedule();

	}

	/**
	 * @param activePart
	 *            the active {@link IWorkbenchPart}
	 * @return the selected {@link IDockerImage} in the given active part of
	 *         <code>null</code> if none was selected
	 */
	public static IDockerImage getSelectedImage(
			final IWorkbenchPart activePart) {
		if (activePart instanceof DockerExplorerView) {
			final ITreeSelection selection = (ITreeSelection) ((DockerExplorerView) activePart)
					.getCommonViewer().getSelection();
			return (IDockerImage) selection.getFirstElement();
		} else if (activePart instanceof DockerImagesView) {
			final IStructuredSelection selection = (IStructuredSelection) (((DockerImagesView) activePart)
					.getSelection());
			if (!selection.isEmpty()) {
				return (IDockerImage) selection.getFirstElement();
			}
		}
		return null;
	}

}
