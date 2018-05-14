/*******************************************************************************
 * Copyright (c) 2016, 2018 Red Hat.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.docker.ui.consoles;

import org.eclipse.jface.action.Action;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.internal.docker.ui.SWTImagesFactory;
import org.eclipse.linuxtools.internal.docker.ui.jobs.DockerComposeStopJob;

/**
 * An {@link Action} to stop the current {@code docker-compose} {@link Process}.
 */
public class DockerComposeStopAction extends Action {

	private final IDockerConnection connection;

	private final String workingDir;

	public DockerComposeStopAction(final IDockerConnection connection,
			final String workingDir) {
		super(ConsoleMessages.getString("DockerComposeStopAction.label")); //$NON-NLS-1$
		this.connection = connection;
		this.workingDir = workingDir;
		setToolTipText(
				ConsoleMessages.getString("DockerComposeStopAction.tooltip")); //$NON-NLS-1$
		setImageDescriptor(SWTImagesFactory.DESC_STOP);
		setDisabledImageDescriptor(SWTImagesFactory.DESC_STOPD);
	}

	@Override
	public void run() {
		new DockerComposeStopJob(connection, workingDir).schedule();
		setEnabled(false);
	}


}
