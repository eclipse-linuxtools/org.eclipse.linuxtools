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
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.swt.widgets.Display;

/**
 * @author jjohnstn
 *
 */
public class DockerImagesContentProvider implements ITreeContentProvider{

	private static final String LoadingImages = "ImagesLoadJob.msg"; //$NON-NLS-1$
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
			if (connection.isImagesLoaded()) {
				return connection.getImages().toArray();
			}
			loadImages(connection);
			return EMPTY;
		} 
		return EMPTY;
	}
	
	/**
	 * Call the {@link IDockerConnection#getImages(boolean)} in a background job
	 * to avoid blocking the UI.
	 * 
	 * @param connection
	 *            the selected {@link DockerConnection}
	 */
	private void loadImages(final IDockerConnection connection) {
		final Job loadImagesJob = new Job(DVMessages.getString(LoadingImages)) {
			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						connection.getImages(true);
						viewer.refresh();
					}
				});
				return Status.OK_STATUS;
			}
		};
		loadImagesJob.schedule();
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
