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
import org.eclipse.linuxtools.internal.docker.ui.views.ImagePushProgressHandler;
import org.eclipse.linuxtools.internal.docker.ui.wizards.ImagePush;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

public class PushImageCommandHandler extends AbstractHandler {

	private final static String PUSH_IMAGE_JOB_TITLE = "ImagePush.title"; //$NON-NLS-1$
	private final static String PUSH_IMAGE_JOB_TASK = "ImagePush.msg"; //$NON-NLS-1$
	private static final String ERROR_PUSHING_IMAGE = "ImagePushError.msg"; //$NON-NLS-1$
	
	private IDockerConnection connection;

	@Override
	public Object execute(final ExecutionEvent event) {
		final IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
		final ImagePush wizard = new ImagePush();
		final boolean pushImage = CommandUtils.openWizard(wizard,
				HandlerUtil.getActiveShell(event));
		if (pushImage) {
			if (activePart instanceof DockerImagesView) {
				connection = ((DockerImagesView) activePart)
						.getConnection();
			}
			performPushImage(wizard);
		}
		return null;
	}
	
	private void performPushImage(final ImagePush wizard) {
		final Job pushImageJob = new Job(DVMessages.getFormattedString(
				PUSH_IMAGE_JOB_TITLE, wizard.getImageTag())) {

			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				final String tag = wizard.getImageTag();
				monitor.beginTask(DVMessages.getString(PUSH_IMAGE_JOB_TASK),
						IProgressMonitor.UNKNOWN);
				// pull the image and let the progress
				// handler refresh the images when done
				try {
					((DockerConnection) connection).pushImage(tag,
							new ImagePushProgressHandler(connection, tag));
				} catch (final DockerException e) {
					Display.getDefault().syncExec(new Runnable() {

						@Override
						public void run() {
							MessageDialog.openError(Display.getCurrent()
									.getActiveShell(), DVMessages
									.getFormattedString(ERROR_PUSHING_IMAGE,
											tag), e.getMessage());

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

		pushImageJob.schedule();

	}

}
