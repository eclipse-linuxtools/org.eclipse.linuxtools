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
import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.IDockerConnection2;
import org.eclipse.linuxtools.docker.core.IDockerContainer;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.docker.core.IDockerImageHierarchyNode;
import org.eclipse.linuxtools.docker.ui.Activator;

/**
 * A {@link Job} to retrieve the hierarchy of an {@link IDockerImage} or an
 * {@link IDockerContainer}
 */
public class RetrieveImageHierarchyJob extends Job {

	private final IDockerConnection2 connection;

	/**
	 * the {@link IDockerImage} or {@link IDockerContainer} for which the
	 * hierarchy needs to be retrieved.
	 */
	private final Object selectedElement;

	/**
	 * the resulting {@link IDockerImageHierarchyNode} that was retrieved while
	 * the job was executed.
	 */
	private IDockerImageHierarchyNode imageHierarchy;

	/**
	 * Constructor
	 * 
	 * @param connection
	 *            the current {@link IDockerConnection2}
	 * @param selectedElement
	 *            the {@link IDockerImage} or {@link IDockerContainer} for which
	 *            the hierarchy needs to be retrieved.
	 */
	public RetrieveImageHierarchyJob(final IDockerConnection2 connection,
			final Object selectedElement) {
		super(JobMessages.getString("RetrieveImageHierarchyJob.title")); //$NON-NLS-1$
		this.connection = connection;
		this.selectedElement = selectedElement;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			monitor.beginTask(
					JobMessages.getString("RetrieveImageHierarchyJob.title"), //$NON-NLS-1$
					1);
			if (selectedElement instanceof IDockerContainer) {
				this.imageHierarchy = connection.resolveImageHierarchy(
						(IDockerContainer) selectedElement);
			} else if (selectedElement instanceof IDockerImage) {
				this.imageHierarchy = connection
						.resolveImageHierarchy((IDockerImage) selectedElement);
			} else {
				if (selectedElement != null) {
					Activator.log(
							new DockerException(JobMessages.getFormattedString(
									"RetrieveImageHierarchyJob.error", //$NON-NLS-1$
									selectedElement.getClass().getName())));
				}
			}
		} finally {
			monitor.done();
		}
		return Status.OK_STATUS;
	}

	/**
	 * @return the {@link IDockerImageHierarchyNode} that was retrieved while the
	 *         job was executed.
	 */
	public IDockerImageHierarchyNode getImageHierarchy() {
		return this.imageHierarchy;
	}

}
