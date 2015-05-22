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
import org.eclipse.linuxtools.internal.docker.ui.views.DockerImagesView;
import org.eclipse.linuxtools.internal.docker.ui.wizards.ImageRemoveTag;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

public class RemoveTagCommandHandler extends AbstractHandler {

	private final static String REMOVE_TAG_JOB_TITLE = "ImageRemoveTagTitle.msg"; //$NON-NLS-1$
	private final static String REMOVE_TAG_MSG = "ImageRemoveTag.msg"; //$NON-NLS-1$
	private static final String ERROR_REMOVING_TAG_IMAGE = "ImageRemoveTagError.msg"; //$NON-NLS-1$
	
	private IDockerConnection connection;
	private IDockerImage image;

	@Override
	public Object execute(final ExecutionEvent event) {
		final IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
		List<IDockerImage> selectedImages = CommandUtils
				.getSelectedImages(activePart);
		if (activePart instanceof DockerImagesView) {
			connection = ((DockerImagesView) activePart).getConnection();
		}
		if (selectedImages.size() != 1 || connection == null)
			return null;
		image = selectedImages.get(0);
		final ImageRemoveTag wizard = new ImageRemoveTag(image);
		final boolean removeTag = CommandUtils.openWizard(wizard,
				HandlerUtil.getActiveShell(event));
		if (removeTag) {
			if (activePart instanceof DockerImagesView) {
				connection = ((DockerImagesView) activePart)
						.getConnection();
			}
			performRemoveTagImage(wizard);
		}
		return null;
	}
	
	private void performRemoveTagImage(final ImageRemoveTag wizard) {
		final Job removeTagImageJob = new Job(
				DVMessages.getString(REMOVE_TAG_JOB_TITLE)) {

			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				final String tag = wizard.getTag();
				monitor.beginTask(DVMessages.getString(REMOVE_TAG_MSG), 2);
				try {
					((DockerConnection) connection).removeTag(tag);
					monitor.worked(1);
					((DockerConnection) connection).getImages(true);
					monitor.worked(1);
				} catch (final DockerException e) {
					Display.getDefault().syncExec(new Runnable() {

						@Override
						public void run() {
							MessageDialog.openError(Display.getCurrent()
									.getActiveShell(), DVMessages
									.getFormattedString(
											ERROR_REMOVING_TAG_IMAGE,
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

		removeTagImageJob.schedule();

	}

}
