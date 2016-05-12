/*******************************************************************************
 * Copyright (c) 2016 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.ui;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerExplorerView;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

/**
 * Singleton class to track current connection set by Docker Explorer View
 * 
 * @author jjohnstn
 *
 */
public class DockerConnectionWatcher implements ISelectionListener {

	private static DockerConnectionWatcher instance;
	private IDockerConnection connection;

	public static DockerConnectionWatcher getInstance() {
		if (instance == null) {
			instance = new DockerConnectionWatcher();
		}
		return instance;
	}

	private DockerConnectionWatcher() {
		// track selection changes in the Docker Explorer view (only)
		PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getSelectionService()
				.addSelectionListener(DockerExplorerView.VIEW_ID, this);

	}

	public void dispose() {
		// stop tracking selection changes in the Docker Explorer view (only)
		PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getSelectionService()
				.removeSelectionListener(DockerExplorerView.VIEW_ID, this);
	}

	/**
	 * Set the current connection
	 * 
	 * @param connection
	 *            new connection to set
	 * 
	 */
	public void setConnection(IDockerConnection connection) {
		this.connection = connection;
	}

	/**
	 * Get the current connection
	 * 
	 * @return the current connection or null if none is set
	 */
	public IDockerConnection getConnection() {
		return this.connection;
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		final ITreeSelection treeSelection = (ITreeSelection) selection;
		if (treeSelection.isEmpty()) {
			setConnection(null);
			return;
		}
		final Object firstSegment = treeSelection.getPaths()[0]
				.getFirstSegment();
		if (firstSegment instanceof IDockerConnection) {
			setConnection((IDockerConnection) firstSegment);
		}
	}
}
