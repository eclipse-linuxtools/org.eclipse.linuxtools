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
package org.eclipse.linuxtools.internal.docker.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.DockerImagePushFailedException;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerProgressDetail;
import org.eclipse.linuxtools.docker.core.IDockerProgressHandler;
import org.eclipse.linuxtools.docker.core.IDockerProgressMessage;

public class DefaultImagePushProgressHandler implements IDockerProgressHandler {

	private final static String IMAGE_UPLOAD_COMPLETE = "ImageUploadComplete.msg"; //$NON-NLS-1$
	private final static String IMAGE_UPLOAD_ALREADY_COMPLETE = "ImageUploadAlreadyComplete.msg"; //$NON-NLS-1$
	private final static String IMAGE_UPLOADING_JOBNAME = "ImageUploadingJobName.msg"; //$NON-NLS-1$
	private final static String IMAGE_UPLOADING_IMAGE = "ImageUploadingImage.msg"; //$NON-NLS-1$
	private final static String IMAGE_UPLOADING = "ImageUploading.msg"; //$NON-NLS-1$

	private String image;
	private DockerConnection connection;

	private Map<String, ProgressJob> progressJobs = new HashMap<>();

	public DefaultImagePushProgressHandler(IDockerConnection connection, String image) {
		this.image = image;
		this.connection = (DockerConnection) connection;
	}

	@Override
	public void processMessage(IDockerProgressMessage message)
			throws DockerException {
		if (message.error() != null) {
			stopAllJobs();
			throw new DockerImagePushFailedException(image, message.error());
		}
		String id = message.id();
		if (id != null) {
			ProgressJob p = progressJobs.get(id);
			if (p == null) {
				String status = message.status();
				if (status.equals(DockerMessages.getString(IMAGE_UPLOAD_COMPLETE))
						|| status.contains(DockerMessages.getString(IMAGE_UPLOAD_ALREADY_COMPLETE))) {
					connection.getImages(true);
				} else {
					ProgressJob newJob = new ProgressJob(
							DockerMessages.getFormattedString(
									IMAGE_UPLOADING_JOBNAME, image),
							DockerMessages.getFormattedString(
									IMAGE_UPLOADING_IMAGE, id));
					// job.setUser(false) will show all pull job (one per image
					// layer) in the progress
					// view but not in multiple dialog
					newJob.setUser(false);
					newJob.setPriority(Job.LONG);
					newJob.schedule();
					progressJobs.put(id, newJob);
				}

			} else {
				String status = message.status();
				if (status.equals(DockerMessages.getString(IMAGE_UPLOAD_COMPLETE))
						|| status.contains(DockerMessages.getString(IMAGE_UPLOAD_ALREADY_COMPLETE))) {
					p.setPercentageDone(100);
					connection.getImages(true);
				} else if (status.startsWith(DockerMessages
						.getString(IMAGE_UPLOADING))) {
					IDockerProgressDetail detail = message.progressDetail();
					if (detail != null) {
						if (detail.current() > 0 && detail.total() > 0) {
							long percentage = (detail.current() * 100)
									/ detail.total();
							p.setPercentageDone((int) percentage);
						}
					}
				}
			}
		}
	}

	private void stopAllJobs() {
		for (ProgressJob j : progressJobs.values()) {
			j.cancel();
		}

	}

}
