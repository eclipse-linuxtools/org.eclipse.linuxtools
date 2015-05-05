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
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerExplorerContentProvider.DockerContainersCategory;
import org.eclipse.swt.widgets.Display;

/**
 * @author xcoulon
 *
 */
public class DockerContainersContentProvider implements ITreeContentProvider{

	private static final Object[] EMPTY = new Object[0];
	private TableViewer viewer;
	
	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
		this.viewer = (TableViewer)viewer;
	}

	@Override
	public Object[] getElements(final Object inputElement) {
		if(inputElement instanceof IDockerConnection) {
			final IDockerConnection connection = (IDockerConnection)inputElement;
			if(connection.isContainersLoaded()) {
				return connection.getContainers().toArray();
			}
			loadContainers(connection);
			return EMPTY;
		} 
		return EMPTY;
	}
	
	/**
	 * Call the {@link IDockerConnection#getContainers(boolean)} in a background job to avoid blocking the UI.
	 * @param containersCategory the selected {@link DockerContainersCategory}
	 */
	private void loadContainers(final IDockerConnection connection) {
		final Job loadContainersJob = new Job("Loading containers...") {
			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				connection.getContainers(true);
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						viewer.refresh();
					}
				});
				return Status.OK_STATUS;
			}
		};
		loadContainersJob.schedule();
	}
	
	@Override
	public Object[] getChildren(Object parentElement) {
		return getElements(parentElement);
	}

	@Override
	public Object getParent(Object element) {
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		return false;
	}

}
