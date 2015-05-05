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
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.linuxtools.internal.docker.ui.views.DVMessages;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerImagesView;
import org.eclipse.linuxtools.internal.docker.ui.views.ImagePullProgressHandler;
import org.eclipse.linuxtools.internal.docker.ui.wizards.ImagePull;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

public class PullImageCommandHandler extends AbstractHandler implements
		IHandler {

	private final static String PULL_IMAGE_JOB_TITLE = "ImagePull.msg"; //$NON-NLS-1$
	private static final String ERROR_PULLING_IMAGE = "ImagePullError.msg"; //$NON-NLS-1$
	
	private IDockerConnection connection;

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
		final ImagePull wizard = new ImagePull();
		final boolean pullImage = CommandUtils.openWizard(wizard,
				HandlerUtil.getActiveShell(event));
		if (pullImage) {
			if (activePart instanceof DockerImagesView) {
				connection = ((DockerImagesView) activePart)
						.getConnection();
			}
			performPullImage(wizard);
		}
		return null;
	}
	
	private void performPullImage(final ImagePull wizard) {
		final Job pullImageJob = new Job(
				DVMessages.getString(PULL_IMAGE_JOB_TITLE)) {

			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				final String id = wizard.getImageId();
				monitor.beginTask(DVMessages.getString(PULL_IMAGE_JOB_TITLE), 1);
				// pull the image and let the progress
				// handler refresh the images when done
				try {
					((DockerConnection) connection).pullImage(id,
							new ImagePullProgressHandler(connection, id));
					monitor.worked(1);
				} catch (final DockerException e) {
					Display.getDefault().syncExec(new Runnable() {

						@Override
						public void run() {
							MessageDialog.openError(Display.getCurrent()
									.getActiveShell(),
									DVMessages.getFormattedString(
											ERROR_PULLING_IMAGE, id), e
											.getMessage());

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

		pullImageJob.schedule();

	}

}
