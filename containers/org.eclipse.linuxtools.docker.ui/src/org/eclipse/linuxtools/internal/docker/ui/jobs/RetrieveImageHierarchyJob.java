/*******************************************************************************
 * Copyright (c) 2016 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.docker.ui.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.docker.core.IDockerImageHiearchyNode;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;

/**
 * A {@link Job} to retrieve the hierarchy of an {@link IDockerImage}
 */
public class RetrieveImageHierarchyJob extends Job {

	/**
	 * the {@link IDockerImage} for which the hierarchy needs to be retrieved.
	 */
	private final IDockerImage dockerImage;

	/**
	 * the {@link IDockerImageHiearchyNode} that was retrieved while the job was
	 * executed.
	 */
	private IDockerImageHiearchyNode imageHierarchy;

	/**
	 * Constructor
	 * 
	 * @param dockerImage
	 *            the {@link IDockerImage} for which the hierarchy needs to be
	 *            retrieved.
	 */
	public RetrieveImageHierarchyJob(final IDockerImage dockerImage) {
		super(JobMessages.getString("RetrieveImageHierarchyJob.title")); //$NON-NLS-1$
		this.dockerImage = dockerImage;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			monitor.beginTask(
					JobMessages.getString("RetrieveImageHierarchyJob.title"),
					1);
			// TODO: remove the cast once the 'resolveImageHierarchy' method has
			// been added in the IDockerConnection interface.
			this.imageHierarchy = ((DockerConnection) dockerImage
					.getConnection())
					.resolveImageHierarchy(dockerImage);
		} finally {
			monitor.done();
		}

		return Status.OK_STATUS;
	}

	/**
	 * @return the {@link IDockerImageHiearchyNode} that was retrieved while the
	 *         job was executed.
	 */
	public IDockerImageHiearchyNode getImageHierarchy() {
		return this.imageHierarchy;
	}

}
