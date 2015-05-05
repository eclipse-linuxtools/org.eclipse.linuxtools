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
package org.eclipse.linuxtools.internal.docker.ui.commands;

import java.io.OutputStream;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerContainerConfig;
import org.eclipse.linuxtools.docker.core.IDockerContainerInfo;
import org.eclipse.linuxtools.docker.core.IDockerHostConfig;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.linuxtools.internal.docker.ui.RunConsole;
import org.eclipse.linuxtools.internal.docker.ui.views.DVMessages;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerImagesView;
import org.eclipse.linuxtools.internal.docker.ui.wizards.ContainerCreate;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

public class CreateContainerCommandHandler extends AbstractHandler implements
		IHandler {

	private final static String CREATE_CONTAINER_JOB_TITLE = "ContainerCreateTitle.msg"; //$NON-NLS-1$
	private final static String CREATE_CONTAINER_MSG = "ContainerCreate.msg"; //$NON-NLS-1$
	private static final String ERROR_CREATING_IMAGE = "ContainerCreateError.msg"; //$NON-NLS-1$
	
	private IDockerConnection connection;
	private IDockerImage image;

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
		List<IDockerImage> selectedImages = CommandUtils
				.getSelectedImages(activePart);
		if (activePart instanceof DockerImagesView) {
			connection = ((DockerImagesView) activePart).getConnection();
		}
		if (selectedImages.size() != 1 || connection == null)
			return null;
		image = selectedImages.get(0);
		final ContainerCreate wizard;
		if (!image.isDangling() && !image.isIntermediateImage())
			wizard = new ContainerCreate(connection, image.repoTags().get(0));
		else
			wizard = new ContainerCreate(connection, image.id()
					.substring(0, 12));
		final boolean tagImage = CommandUtils.openWizard(wizard,
				HandlerUtil.getActiveShell(event));
		if (tagImage) {
			if (activePart instanceof DockerImagesView) {
				connection = ((DockerImagesView) activePart)
						.getConnection();
			}
			performCreateContainer(wizard);
		}
		return null;
	}
	
	private void performCreateContainer(final ContainerCreate wizard) {
		final Job createContainerJob = new Job(
				DVMessages.getString(CREATE_CONTAINER_JOB_TITLE)) {

			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				final IDockerContainerConfig config = wizard.getConfig();
				final IDockerHostConfig hostConfig = wizard.getHostConfig();
				final String image = wizard.getImageId();
				monitor.beginTask(DVMessages.getString(CREATE_CONTAINER_MSG), 4);
				// pull the image and let the progress
				// handler refresh the images when done
				try {
					final String containerId = ((DockerConnection) connection)
							.createContainer(config);
					monitor.worked(1);
					IDockerContainerInfo info = ((DockerConnection) connection)
							.getContainerInfo(containerId);
					String name = info.name();
					if (name.startsWith("/")) //$NON-NLS-1$
						name = name.replaceFirst("/", ""); //$NON-NLS-1$ //$NON-NLS-2$
					monitor.worked(1);
					OutputStream stream = null;
					RunConsole rc = RunConsole.findConsole(containerId,
							RunConsole.DEFAULT_ID, name);
					rc.attachToConsole(connection);
					monitor.worked(1);
					if (rc != null) {
						stream = rc.getOutputStream();
					}
					final OutputStream logstream = stream;
					((DockerConnection) connection).startContainer(containerId,
							hostConfig, logstream);
					monitor.worked(1);
				} catch (final DockerException e) {
					Display.getDefault().syncExec(new Runnable() {

						@Override
						public void run() {
							MessageDialog.openError(Display.getCurrent()
									.getActiveShell(), DVMessages
									.getFormattedString(ERROR_CREATING_IMAGE,
											image), e.getMessage());

						}

					});
					// for now
				} catch (InterruptedException e) {
					// do nothing
				} finally {
					monitor.done();
				}
				return Status.OK_STATUS;
			}

		};

		createContainerJob.schedule();

	}

}
