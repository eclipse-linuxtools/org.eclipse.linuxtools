/*******************************************************************************
 * Copyright (c) 2015, 2022 Red Hat Inc. and others.
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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.docker.core.AbstractRegistry;
import org.eclipse.linuxtools.docker.core.DockerCertificateException;
import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.DockerOperationCancelledException;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IRegistryAccount;
import org.eclipse.linuxtools.docker.ui.wizards.ImageSearch;
import org.eclipse.linuxtools.internal.docker.core.DefaultImagePullProgressHandler;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.linuxtools.internal.docker.ui.views.DVMessages;
import org.eclipse.linuxtools.internal.docker.ui.wizards.ImagePull;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Command handler that opens the {@link ImageSearch} wizard and pulls the
 * selected image in background on completion.
 *
 */
public class PullImageCommandHandler extends AbstractHandler {

	private final static String PULL_IMAGE_JOB_TITLE = "ImagePull.title"; //$NON-NLS-1$
	private final static String PULL_IMAGE_JOB_TASK = "ImagePull.msg"; //$NON-NLS-1$
	private static final String ERROR_PULLING_IMAGE = "ImagePullError.msg"; //$NON-NLS-1$
	private static final String ERROR_PULLING_IMAGE_NO_CONNECTION = "command.pullImage.failure.no_connection"; //$NON-NLS-1$
	private static final String MISSING_CONNECTION = "missing_connection"; //$NON-NLS-1$

	@Override
	public Object execute(final ExecutionEvent event) {
		final IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
		final IDockerConnection connection = CommandUtils
				.getCurrentConnection(activePart);
		if (connection == null) {
			MessageDialog.openError(
					PlatformUI.getWorkbench().getActiveWorkbenchWindow()
							.getShell(),
					CommandMessages.getString(MISSING_CONNECTION),
					CommandMessages
							.getString(ERROR_PULLING_IMAGE_NO_CONNECTION));
		} else {
			final ImagePull wizard = new ImagePull(connection);
			final boolean pullImage = CommandUtils.openWizard(wizard,
					HandlerUtil.getActiveShell(event));
			if (pullImage) {
				performPullImage(connection, wizard.getSelectedImageName(),
						// TODO: remove cast once AbstractRegistry methods are
						// part of the IRegistry interface
						(AbstractRegistry) wizard.getSelectedRegistryAccount());
			}
		}
		return null;
	}

	private void performPullImage(final IDockerConnection connection, final String imageName,
			final AbstractRegistry registry) {
		final Job pullImageJob = new Job(DVMessages.getFormattedString(PULL_IMAGE_JOB_TITLE, imageName)) {

			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				final DockerConnection dconn = (DockerConnection) connection;
				monitor.beginTask(DVMessages.getString(PULL_IMAGE_JOB_TASK), IProgressMonitor.UNKNOWN);
				// pull the image and let the progress
				// handler refresh the images when done
				try {
					if (registry == null || registry.isDockerHubRegistry()) {
						dconn.pullImage(imageName,
								new DefaultImagePullProgressHandler(connection, imageName, monitor));
					} else {
						String fullImageName = registry.getServerHost() + '/' + imageName;
						if (registry instanceof IRegistryAccount account) {
							dconn.pullImageWithHandler(fullImageName, account,
									new DefaultImagePullProgressHandler(connection, fullImageName, monitor));
						} else {
							dconn.pullImage(fullImageName,
									new DefaultImagePullProgressHandler(connection, fullImageName, monitor));
						}
					}
				} catch (final DockerOperationCancelledException e) {
					// Cancelled by user. Do nothing
				} catch (InterruptedException | DockerCertificateException e) {
					// do nothing
				} catch (final DockerException e) {
					Display.getDefault()
							.syncExec(() -> MessageDialog.openError(
									PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
									DVMessages.getFormattedString(ERROR_PULLING_IMAGE, imageName), e.getMessage()));
					// for now
				} finally {
					monitor.done();
				}
				return Status.OK_STATUS;
			}

		};

		pullImageJob.schedule();

	}

}
