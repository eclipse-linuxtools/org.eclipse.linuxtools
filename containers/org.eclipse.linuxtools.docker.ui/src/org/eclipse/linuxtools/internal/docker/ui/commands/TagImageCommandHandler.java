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

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.linuxtools.internal.docker.ui.views.DVMessages;
import org.eclipse.linuxtools.internal.docker.ui.wizards.ImageTag;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

public class TagImageCommandHandler extends AbstractHandler {

	private final static String TAG_IMAGE_JOB_TITLE = "ImageTagTitle.msg"; //$NON-NLS-1$
	private final static String TAG_IMAGE_MSG = "ImageTag.msg"; //$NON-NLS-1$
	private static final String ERROR_TAGGING_IMAGE = "ImageTagError.msg"; //$NON-NLS-1$
	
	@Override
	public Object execute(final ExecutionEvent event) {
		final IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
		final List<IDockerImage> selectedImages = CommandUtils
				.getSelectedImages(activePart);
		final IDockerConnection connection = CommandUtils
				.getCurrentConnection(activePart);
		if (selectedImages.size() != 1 || connection == null) {
			return null;
		}
		final IDockerImage image = selectedImages.get(0);
		final ImageTag wizard = new ImageTag();
		final boolean tagImage = CommandUtils.openWizard(wizard,
				HandlerUtil.getActiveShell(event));
		if (tagImage) {
			performTagImage(connection, image, wizard.getTag());
		}
		return null;
	}
	
	private void performTagImage(final IDockerConnection connection,
			final IDockerImage image, final String tag) {
		final Job tagImageJob = new Job(
				DVMessages.getString(TAG_IMAGE_JOB_TITLE)) {

			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				monitor.beginTask(
						DVMessages.getFormattedString(TAG_IMAGE_MSG, tag), 2);
				// tag the image and let the progress
				// handler refresh the images when done
				try {
					((DockerConnection) connection).tagImage(image.id(), tag);
					monitor.worked(1);
					((DockerConnection) connection).getImages(true);
					monitor.worked(1);
				} catch (final DockerException e) {
					Display.getDefault().syncExec(new Runnable() {
						@Override
						public void run() {
							MessageDialog.openError(Display.getCurrent()
									.getActiveShell(), DVMessages
									.getFormattedString(ERROR_TAGGING_IMAGE,
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
		tagImageJob.schedule();
	}

}
