/*******************************************************************************
 * Copyright (c) 2016, 2020 Red Hat Inc. and others.
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
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerContainer;
import org.eclipse.linuxtools.docker.ui.wizards.ImageSearch;
import org.eclipse.linuxtools.internal.docker.core.ContainerFileProxy;
import org.eclipse.linuxtools.internal.docker.ui.jobs.CopyFromDockerJob;
import org.eclipse.linuxtools.internal.docker.ui.jobs.CopyFromDockerJob.CopyType;
import org.eclipse.linuxtools.internal.docker.ui.wizards.ContainerCopyFrom;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Command handler that opens the {@link ImageSearch} wizard and pulls the
 * selected image in background on completion.
 *
 */
public class CopyFromContainerCommandHandler extends AbstractHandler {

	private static final String ERROR_COPYING_FROM_CONTAINER_NO_CONNECTION = "command.copyfromcontainer.failure.no_connection"; //$NON-NLS-1$
	private static final String MISSING_CONNECTION = "missing_connection"; //$NON-NLS-1$

	@Override
	public Object execute(final ExecutionEvent event) {
		final IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
		final IDockerConnection connection = CommandUtils
				.getCurrentConnection(activePart);
		final List<IDockerContainer> selectedContainers = CommandUtils
				.getSelectedContainers(activePart);
		if (selectedContainers.size() != 1) {
			return null;
		}
		final IDockerContainer container = selectedContainers.get(0);
		if (connection == null) {
			MessageDialog.openError(
					PlatformUI.getWorkbench().getActiveWorkbenchWindow()
							.getShell(),
					CommandMessages.getString(MISSING_CONNECTION),
					CommandMessages
							.getString(
									ERROR_COPYING_FROM_CONTAINER_NO_CONNECTION));
		} else {
			final ContainerCopyFrom wizard = new ContainerCopyFrom(connection,
					container);
			final boolean copyFromContainer = CommandUtils.openWizard(wizard,
					HandlerUtil.getActiveShell(event));
			if (copyFromContainer) {
				performCopyFromContainer(connection, container,
						wizard.getTarget(), wizard.getSources());
			}
		}
		return null;
	}



	private void performCopyFromContainer(final IDockerConnection connection, final IDockerContainer container,
			final String target, final List<ContainerFileProxy> files) {
		Set<Path> copyset = files.stream().map(x -> new Path(x.getFullPath())).collect(Collectors.toSet());
		final Job copyFromContainerJob = new CopyFromDockerJob(connection, CopyType.Container, container.id(),
				copyset, new Path(target));
		copyFromContainerJob.schedule();
	}

}
