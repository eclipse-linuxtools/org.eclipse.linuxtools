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

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerContainer;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.linuxtools.internal.docker.ui.views.DVMessages;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerContainersView;
import org.eclipse.linuxtools.internal.docker.ui.wizards.ContainerCommit;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

public class CommitContainerCommandHandler extends AbstractHandler {

	private final static String COMMIT_CONTAINER_JOB_TITLE = "ContainerCommitTitle.msg"; //$NON-NLS-1$
	private final static String COMMIT_CONTAINER_MSG = "ContainerCommit.msg"; //$NON-NLS-1$
	private static final String ERROR_COMMITTING_CONTAINER = "ContainerCommitError.msg"; //$NON-NLS-1$
	
	private IDockerConnection connection;
	private IDockerContainer container;

	@Override
	public Object execute(final ExecutionEvent event) {
		final IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
		List<IDockerContainer> selectedContainers = CommandUtils
				.getSelectedContainers(activePart);
		if (activePart instanceof DockerContainersView) {
			connection = ((DockerContainersView) activePart).getConnection();
		}
		if (selectedContainers.size() != 1 || connection == null)
			return null;
		container = selectedContainers.get(0);
		final ContainerCommit wizard = new ContainerCommit(container.id());
		final boolean commitContainer = CommandUtils.openWizard(wizard,
				HandlerUtil.getActiveShell(event));
		if (commitContainer) {
			performCommitContainer(wizard);
		}
		return null;
	}
	
	private void performCommitContainer(final ContainerCommit wizard) {
		final Job commitContainerJob = new Job(
				DVMessages.getString(COMMIT_CONTAINER_JOB_TITLE)) {

			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				final String tag = wizard.getTag();
				final String repo = wizard.getRepo();
				final String author = wizard.getAuthor();
				final String comment = wizard.getComment();
				monitor.beginTask(DVMessages.getString(COMMIT_CONTAINER_MSG), 1);
				// commit the Container and then update the list of Images
				try {
					((DockerConnection) connection).commitContainer(
							container.id(), repo, tag, comment, author);
					monitor.worked(1);
				} catch (DockerException e) {
					MessageDialog.openError(Display.getCurrent()
							.getActiveShell(), DVMessages.getFormattedString(
							ERROR_COMMITTING_CONTAINER, tag), e.getMessage());
				} finally {
					monitor.done();
				}
				return Status.OK_STATUS;
			}

		};

		commitContainerJob.schedule();

	}

}
