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
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerContainer;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.internal.docker.ui.views.DVMessages;
import org.eclipse.swt.widgets.Display;

/**
 * Command handler to kill all the selected {@link IDockerContainer}
 * 
 * @author jjohnstn
 *
 */
public class RemoveImagesCommandHandler extends BaseImagesCommandHandler {

	private static final String IMAGES_DELETE_MSG = "ImagesRemove.msg"; //$NON-NLS-1$
	private static final String IMAGE_DELETE_MSG = "ImageRemove.msg"; //$NON-NLS-1$
	private static final String IMAGE_DELETE_ERROR_MSG = "ImageRemoveError.msg"; //$NON-NLS-1$
	private static final String IMAGE_DELETE_CONFIRM = "ImageDeleteConfirm.msg"; //$NON-NLS-1$
	private static final String IMAGE_DELETE_LIST = "ImageDeleteList.msg"; //$NON-NLS-1$

	@Override
	void executeInJob(final IDockerImage image,
			final IDockerConnection connection) {
		try {
			connection.removeImage(image.id());
		} catch (DockerException | InterruptedException e) {
			if (!image.isDangling() && !image.isIntermediateImage()) {
				final String errorMessage = DVMessages.getFormattedString(
						IMAGE_DELETE_ERROR_MSG, image.repoTags().get(0));
				openError(errorMessage, e);

			} else {
				final String errorMessage = DVMessages.getFormattedString(
						IMAGE_DELETE_ERROR_MSG, image.id().substring(0, 8));
				openError(errorMessage, e);
			}
		} finally {
			// always get images as we sometimes get errors on intermediate
			// images
			// being removed but we will remove some top ones successfully
			connection.getImages(true);
		}
	}

	@Override
	String getJobName(final List<IDockerImage> selectedImages) {
		return DVMessages.getString(IMAGES_DELETE_MSG);
	}

	@Override
	String getTaskName(final IDockerImage image) {
		if (!image.isDangling() && !image.isIntermediateImage())
			return DVMessages.getFormattedString(IMAGE_DELETE_MSG, image
					.repoTags().get(0));

		return DVMessages.getFormattedString(IMAGE_DELETE_MSG, image.id()
				.substring(0, 8));
	}
	
	private class DialogResponse {
		private boolean response;

		public boolean getResponse() {
			return response;
		}

		public void setResponse(boolean value) {
			response = value;
		}
	}

	@Override
	boolean confirmed(List<IDockerImage> selectedImages) {
		// ask for confirmation before deleting images
		List<String> imagesToRemove = new ArrayList<>();
		for (IDockerImage image : selectedImages) {
			// use repo tags if present, otherwise truncate ids
			// to 8 characters so they show up
			// reasonably in confirmation dialog...don't
			// think this will ever cause an issue
			if (!image.isDangling() && !image.isIntermediateImage())
				imagesToRemove.add(image.repoTags().get(0));
			else
				imagesToRemove.add(image.id().substring(0, 8));
		}
		final List<String> names = imagesToRemove;
		final DialogResponse response = new DialogResponse();
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				boolean result = MessageDialog.openConfirm(
						Display.getDefault().getActiveShell(),
						DVMessages.getString(IMAGE_DELETE_CONFIRM),
						DVMessages.getFormattedString(IMAGE_DELETE_LIST,
								names.toString()));
				response.setResponse(result);
			}
		});
		return response.getResponse();
	}

}
