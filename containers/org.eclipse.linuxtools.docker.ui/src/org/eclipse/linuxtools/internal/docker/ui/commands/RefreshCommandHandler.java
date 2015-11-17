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

import static org.eclipse.linuxtools.internal.docker.ui.commands.CommandUtils.getCurrentConnection;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerContainer;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.internal.docker.ui.views.DVMessages;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerContainersView;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerExplorerContentProvider.DockerContainersCategory;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerExplorerContentProvider.DockerImagesCategory;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerExplorerView;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerImagesView;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * The command handler that manages the refreshing of all
 * {@link IDockerContainer}s and {@link IDockerImage}s.
 */
public class RefreshCommandHandler extends AbstractHandler {

	public final static String CONTAINERS_REFRESH_MSG = "ContainersRefresh.msg"; //$NON-NLS-1$
	public final static String IMAGES_REFRESH_MSG = "ImagesRefresh.msg"; //$NON-NLS-1$

	@Override
	public Object execute(ExecutionEvent event) {
		final IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
		final Job job = getRefreshJob(activePart);
		if (job != null) {
			job.setPriority(Job.LONG);
			job.setUser(true);
			job.schedule();
		}
		return null;
	}

	private Job getRefreshJob(final IWorkbenchPart activePart) {
		final IDockerConnection connection = getCurrentConnection(activePart);
		if (activePart instanceof DockerImagesView) {
			return getRefreshImagesJob(connection);
		} else if (activePart instanceof DockerContainersView) {
			return getRefreshContainersJob(connection);
		} else if (activePart instanceof DockerExplorerView) {
			DockerExplorerView dockerExplorerView = (DockerExplorerView) activePart;
			final ITreeSelection selection = dockerExplorerView
					.getCommonViewer().getStructuredSelection();
			if (selection
					.getFirstElement() instanceof DockerContainersCategory) {
				return getRefreshContainersJob(connection);
			} else if (selection
					.getFirstElement() instanceof DockerImagesCategory) {
				return getRefreshImagesJob(connection);
			}
		}
		return null;
	}

	private Job getRefreshContainersJob(final IDockerConnection connection) {
		if (connection == null) {
			return null;
		}
		final Job job = new Job(DVMessages.getString(CONTAINERS_REFRESH_MSG)) {
			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				monitor.beginTask(DVMessages.getString(CONTAINERS_REFRESH_MSG),
						1);
				connection.getContainers(true);
				monitor.worked(1);
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		return job;
	}

	private Job getRefreshImagesJob(final IDockerConnection connection) {
		if (connection == null) {
			return null;
		}
		final Job job = new Job(DVMessages.getString(IMAGES_REFRESH_MSG)) {
			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				monitor.beginTask(DVMessages.getString(IMAGES_REFRESH_MSG), 1);
				connection.getImages(true);
				monitor.worked(1);
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		return job;
	}

}
