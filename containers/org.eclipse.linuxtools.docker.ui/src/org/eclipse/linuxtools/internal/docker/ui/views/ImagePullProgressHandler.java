/*******************************************************************************
 * Copyright (c) 2014 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.ui.views;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.DockerImagePullFailedException;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerProgressDetail;
import org.eclipse.linuxtools.docker.core.IDockerProgressHandler;
import org.eclipse.linuxtools.docker.core.IDockerProgressMessage;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.linuxtools.internal.docker.ui.ProgressJob;

public class ImagePullProgressHandler implements IDockerProgressHandler {

	private final static String IMAGE_DOWNLOAD_COMPLETE = "ImageDownloadComplete.msg"; //$NON-NLS-1$
	private final static String IMAGE_DOWNLOADING_JOBNAME = "ImageDownloadingJobName.msg"; //$NON-NLS-1$
	private final static String IMAGE_DOWNLOADING_IMAGE = "ImageDownloadingImage.msg"; //$NON-NLS-1$
	private final static String IMAGE_DOWNLOADING = "ImageDownloading.msg"; //$NON-NLS-1$
	private final static String IMAGE_PULLING = "ImagePulling.msg"; //$NON-NLS-1$
	private final static String IMAGE_PULL_COMPLETE = "ImagePullComplete.msg"; //$NON-NLS-1$
	private final static String IMAGE_DOWNLOADING_ALREADY_EXISTS = "ImageDownloadingAlreadyExists.msg"; //$NON-NLS-1$
	private final static String IMAGE_DOWNLOADING_VERIFIED = "ImageDownloadingVerified.msg"; //$NON-NLS-1$

	private String image;
	private DockerConnection connection;

	private Map<String, ProgressJob> progressJobs = new HashMap<>();

	public ImagePullProgressHandler(IDockerConnection connection, String image) {
		this.image = image;
		this.connection = (DockerConnection) connection;
	}

	@Override
	public void processMessage(IDockerProgressMessage message)
			throws DockerException {
		if (message.error() != null) {
			stopAllJobs();
			throw new DockerImagePullFailedException(image, message.error());
		}
		String id = message.id();
		if (id != null) {
			ProgressJob p = progressJobs.get(id);
			if (p == null) {
				String status = message.status();
				if (status.contains(DVMessages.getString(IMAGE_PULLING))) {
					// do nothing
				} else if (status
						.equals(DVMessages.getString(IMAGE_DOWNLOAD_COMPLETE))
						|| status.contains(DVMessages
								.getString(IMAGE_DOWNLOADING_ALREADY_EXISTS))
						|| status.contains(DVMessages
								.getString(IMAGE_DOWNLOADING_VERIFIED))
						|| status.equals(
								DVMessages.getString(IMAGE_PULL_COMPLETE))) {
					// an image is fully loaded, update the image list
					connection.getImages(true);
				} else if (status
						.startsWith(DVMessages.getString(IMAGE_DOWNLOADING))) {
					// we have a new download in progress, track it
					ProgressJob newJob = new ProgressJob(
							DVMessages.getFormattedString(
									IMAGE_DOWNLOADING_JOBNAME, image),
							DVMessages.getFormattedString(
									IMAGE_DOWNLOADING_IMAGE, id));
					newJob.setUser(true);
					newJob.setPriority(Job.LONG);
					newJob.schedule();
					progressJobs.put(id, newJob);
				}

			} else {
				String status = message.status();
				if (status.equals(DVMessages.getString(IMAGE_DOWNLOAD_COMPLETE))
						|| status.contains(DVMessages
								.getString(IMAGE_DOWNLOADING_ALREADY_EXISTS))
						|| status.contains(DVMessages
								.getString(IMAGE_DOWNLOADING_VERIFIED))) {
					p.setPercentageDone(100);
					connection.getImages(true);
				} else if (status
						.startsWith(DVMessages.getString(IMAGE_DOWNLOADING))) {
					IDockerProgressDetail detail = message.progressDetail();
					if (detail != null) {
						if (detail.current() > 0) {
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
