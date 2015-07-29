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

package org.eclipse.linuxtools.internal.docker.ui.views;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.swt.widgets.Display;

/**
 * @author xcoulon
 *
 */
public class DockerExplorerContentProvider implements ITreeContentProvider {

	private final Object[] EMPTY = new Object[0];
	
	private TreeViewer viewer;
	
	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
		this.viewer = (TreeViewer)viewer;
	}

	@Override
	public Object[] getElements(final Object inputElement) {
		if (inputElement instanceof DockerConnectionManager) {
			final DockerConnectionManager connectionManager = (DockerConnectionManager) inputElement;
			return connectionManager.getConnections();
		}
		return EMPTY;
	}

	@Override
	public Object[] getChildren(final Object parentElement) {
		if (parentElement instanceof IDockerConnection) {
			final IDockerConnection dockerConnection = (IDockerConnection) parentElement;
			return new Object[] { new DockerImagesCategory(dockerConnection),
					new DockerContainersCategory(dockerConnection) };
		} else if (parentElement instanceof DockerContainersCategory) {
			final DockerContainersCategory containersCategory = (DockerContainersCategory) parentElement;
			final IDockerConnection connection = containersCategory.getConnection();
			if(connection.isContainersLoaded()) {
				return connection.getContainers().toArray();
			}
			loadContainers(containersCategory);
			return new Object[] { new LoadingStub(containersCategory) };
		} else if (parentElement instanceof DockerImagesCategory) {
			final DockerImagesCategory imagesCategory = (DockerImagesCategory) parentElement;
			final IDockerConnection connection = imagesCategory.getConnection();
			if(connection.isImagesLoaded()) {
				return connection.getImages().toArray();
			}
			loadImages(imagesCategory);
			return new Object[] { new LoadingStub(imagesCategory) };
		}
		return EMPTY;
	}

	/**
	 * Call the {@link IDockerConnection#getContainers(boolean)} in a background job to avoid blocking the UI.
	 * @param containersCategory the selected {@link DockerContainersCategory}
	 */
	private void loadContainers(final DockerContainersCategory containersCategory) {
		final Job loadContainersJob = new Job("Loading containers...") {
			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				containersCategory.getConnection().getContainers(true);
				return Status.OK_STATUS;
			}
		};
		loadContainersJob.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(final IJobChangeEvent event) {
				event.getResult();
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						refreshTarget(containersCategory);
					}
				});
			}
		});
		loadContainersJob.schedule();
	}

	/**
	 * Call the {@link IDockerConnection#getImages(boolean)} in a background job to avoid blocking the UI.
	 * @param imagesCategory the selected {@link DockerImagesCategory}
	 */
	private void loadImages(final DockerImagesCategory imagesCategory) {
		final Job loadImagesJob = new Job("Loading images...") {
			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				imagesCategory.getConnection().getImages(true);
				return Status.OK_STATUS;
			}
		};
		loadImagesJob.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(final IJobChangeEvent event) {
				refreshTarget(imagesCategory);
			}
		});
		loadImagesJob.schedule();
	}
	
	@Override
	public Object getParent(final Object element) {
		if (element instanceof DockerImagesCategory) {
			return ((DockerImagesCategory) element).getConnection();
		} else if (element instanceof DockerContainersCategory) {
			return ((DockerContainersCategory) element).getConnection();
		}
		return null;
	}

	@Override
	public boolean hasChildren(final Object element) {
		return (element instanceof IDockerConnection || element instanceof DockerContainersCategory || element instanceof DockerImagesCategory);
	}
	
	/**
	 * Refresh the whole content tree for the <strong>given target node and all
	 * its subelements</strong>.
	 * 
	 * @param target
	 *            the node to refresh
	 */
	private void refreshTarget(final Object target) {
		// this piece of code must run in an async manner to avoid reentrant
		// call while viewer is busy.
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				if (viewer != null) {
					TreePath[] treePaths = viewer.getExpandedTreePaths();
					viewer.refresh(target, true);
					viewer.setExpandedTreePaths(treePaths);
				}
			}
		});
	}

	public static class DockerImagesCategory {

		private final IDockerConnection connection;

		/**
		 * @param connection
		 *            - Docker connection
		 */
		public DockerImagesCategory(final IDockerConnection connection) {
			this.connection = connection;
		}

		public IDockerConnection getConnection() {
			return connection;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((connection == null) ? 0 : connection.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			DockerImagesCategory other = (DockerImagesCategory) obj;
			if (connection == null) {
				if (other.connection != null)
					return false;
			} else if (!connection.equals(other.connection))
				return false;
			return true;
		}

	}

	public static class DockerContainersCategory {

		private final IDockerConnection connection;

		/**
		 * @param connection
		 *            - Docker connection
		 */
		public DockerContainersCategory(final IDockerConnection connection) {
			this.connection = connection;
		}

		public IDockerConnection getConnection() {
			return connection;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((connection == null) ? 0 : connection.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			DockerContainersCategory other = (DockerContainersCategory) obj;
			if (connection == null) {
				if (other.connection != null)
					return false;
			} else if (!connection.equals(other.connection))
				return false;
			return true;
		}

	}
	
	public static class LoadingStub {
		
		private final Object element;
		
		public LoadingStub(final Object element) {
			this.element = element;
		}
		
		public Object getElement() {
			return element;
		}
	}

}
