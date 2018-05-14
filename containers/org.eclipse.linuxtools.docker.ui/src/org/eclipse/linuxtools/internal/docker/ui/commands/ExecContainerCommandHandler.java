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
package org.eclipse.linuxtools.internal.docker.ui.commands;

import java.util.List;

import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerContainer;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.osgi.util.NLS;

public class ExecContainerCommandHandler extends BaseContainersCommandHandler {

	@Override
	String getJobName(List<IDockerContainer> selectedContainers) {
		return Messages.ExecContainerCommandHandler_0;
	}

	@Override
	String getTaskName(IDockerContainer container) {
		return NLS.bind(Messages.ExecContainerCommandHandler_1, container.id());
	}

	@Override
	void executeInJob(IDockerContainer container,
			IDockerConnection connection) {
		try {
			((DockerConnection) connection).execShell(container.id());
		} catch (DockerException e) {
			String errorMessage = NLS.bind(Messages.ExecContainerCommandHandler_2, container.id());
			openError(errorMessage, e);
		}
	}

}
