/*******************************************************************************
 * Copyright (c) 2014, 2016 Red Hat Inc. and others.
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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.docker.core.DockerContainerNotFoundException;
import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.EnumDockerLoggingStatus;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerContainer;
import org.eclipse.linuxtools.docker.core.IDockerContainerConfig;
import org.eclipse.linuxtools.docker.core.IDockerHostConfig;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.linuxtools.internal.docker.ui.consoles.RunConsole;
import org.eclipse.linuxtools.internal.docker.ui.launch.LaunchConfigurationUtils;
import org.eclipse.linuxtools.internal.docker.ui.views.DVMessages;
import org.eclipse.linuxtools.internal.docker.ui.wizards.ImageRun;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * @author xcoulon
 *
 */
public class RunImageCommandHandler extends AbstractHandler {

	private static final String ERROR_CREATING_CONTAINER = "ContainerCreateError.msg"; //$NON-NLS-1$
	private static final String ERROR_REMOVING_CONTAINER = "ContainerRemoveError.msg"; //$NON-NLS-1$

	@Override
	public Object execute(final ExecutionEvent event) {
		final IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
		final IDockerImage selectedImage = CommandUtils
				.getSelectedImage(activePart);
		if (selectedImage == null) {
			Activator.log(new DockerException(
					DVMessages.getString("RunImageUnableToRetrieveError.msg"))); //$NON-NLS-1$
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
					runImage(selectedImage, containerConfig,
							hostConfig, wizard.getDockerContainerName(),
							wizard.removeWhenExits());
				}
			} catch (DockerException | CoreException e) {
				Activator.log(e);
			}
		}
		return null;
	}

	/**
	 * Run the given {@link IDockerImage} with the given settings
	 * 
	 * @param image
	 * @param containerConfig
	 * @param hostConfig
	 * @param containerName
	 * @param removeWhenExits
	 */
	public static void runImage(final IDockerImage image,
			final IDockerContainerConfig containerConfig,
			final IDockerHostConfig hostConfig, final String containerName,
			final boolean removeWhenExits) {
		final IDockerConnection connection = image.getConnection();
		if (containerConfig.tty()) {
			// show the console view
			Display.getDefault().asyncExec(() -> {
				try {
					PlatformUI.getWorkbench().getActiveWorkbenchWindow()
							.getActivePage()
							.showView(IConsoleConstants.ID_CONSOLE_VIEW);
				} catch (Exception e) {
					Activator.log(e);
				}
			});
		}

		// Create the container in a non-UI thread.
		final Job runImageJob = new Job(
				DVMessages.getString("RunImageCreateContainer.job")) { //$NON-NLS-1$

			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				monitor.beginTask(
						DVMessages.getString("RunImageRunningTask.msg"), 2); //$NON-NLS-1$
				String containerId = null;
				RunConsole console = null;
				try {
					final SubMonitor createContainerMonitor = SubMonitor
							.convert(monitor, 1);
					// create the container
					createContainerMonitor.beginTask(
							DVMessages.getString(
									"RunImageCreatingContainerTask.msg"), //$NON-NLS-1$
							1);
					containerId = ((DockerConnection) connection)
							.createContainer(containerConfig, hostConfig, containerName);
					final IDockerContainer container = ((DockerConnection) connection)
							.getContainer(containerId);
					createContainerMonitor.done();
					// abort if operation was cancelled
					if (monitor.isCanceled()) {
						return Status.CANCEL_STATUS;
					}
					// start the container
					final SubMonitor startContainerMonitor = SubMonitor
							.convert(monitor, 1);
					startContainerMonitor.beginTask(DVMessages
							.getString("RunImageStartingContainerTask.msg"), 1); //$NON-NLS-1$
					console = getRunConsole(connection,	container);
					if (console != null) {
						// if we are auto-logging, show the console
						console.showConsole();
						((DockerConnection) connection).startContainer(
								containerId, console.getOutputStream());
					} else {
						((DockerConnection) connection)
								.startContainer(containerId, null);
					}
					startContainerMonitor.done();
					// create a launch configuration from the container
					LaunchConfigurationUtils.createRunImageLaunchConfiguration(image,
							containerConfig,
							hostConfig, containerName,
							removeWhenExits);
				} catch (final DockerException | InterruptedException e) {
					if (console != null) {
						RunConsole.removeConsole(console);
					}
					Display.getDefault().syncExec(() -> MessageDialog.openError(
							PlatformUI.getWorkbench().getActiveWorkbenchWindow()
									.getShell(),
							DVMessages.getFormattedString(
									ERROR_CREATING_CONTAINER,
									containerConfig.image()),
							e.getMessage()));
				} finally {
					final String tmpContainerId = containerId;
					if (removeWhenExits) {
						final Job waitContainerJob = new Job(
								CommandMessages.getString(
										"RunImageCommandHandler.waitContainer.label")) { //$NON-NLS-1$
							@Override
							protected IStatus run(IProgressMonitor monitor) {
								try {
									if (tmpContainerId != null) {
										// Wait for the container to finish
										((DockerConnection) connection)
												.waitForContainer(tmpContainerId);
										// Drain the logging thread before we remove the
										// container
										((DockerConnection) connection)
												.stopLoggingThread(tmpContainerId);
										while (((DockerConnection) connection)
												.loggingStatus(
														tmpContainerId) == EnumDockerLoggingStatus.LOGGING_ACTIVE) {
											Thread.sleep(1000);
										}
									}
								} catch (DockerContainerNotFoundException e) {
									// container not created correctly
									return Status.OK_STATUS;
								} catch (DockerException | InterruptedException e) {
									// ignore any errors in waiting for container or
									// draining log
								}
								try {
									// try and remove the container if it was created
									if (tmpContainerId != null)
										((DockerConnection) connection)
												.removeContainer(tmpContainerId);
								} catch (DockerException | InterruptedException e) {
									final String id = tmpContainerId;
									Display.getDefault()
											.syncExec(() -> MessageDialog.openError(
													PlatformUI.getWorkbench()
															.getActiveWorkbenchWindow()
															.getShell(),
													DVMessages.getFormattedString(
															ERROR_REMOVING_CONTAINER,
															id),
													e.getMessage()));
								}
								return Status.OK_STATUS;
							}
						};
						// Do not display this job in the UI
						waitContainerJob.setSystem(true);
						waitContainerJob.schedule();
					}

					monitor.done();
				}
				return Status.OK_STATUS;
			}
		};
		runImageJob.schedule();

	}

}
