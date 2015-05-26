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

package org.eclipse.linuxtools.internal.docker.ui.commands;

import static org.eclipse.linuxtools.internal.docker.ui.commands.CommandUtils.getCurrentConnection;
import static org.eclipse.linuxtools.internal.docker.ui.commands.CommandUtils.getSelectedContainers;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerContainer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Command handler to kill all the selected {@link IDockerContainer}
 * 
 * @author xcoulon
 *
 */
public abstract class BaseContainersCommandHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) {
		final IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
		final List<IDockerContainer> selectedContainers = getSelectedContainers(
				activePart);
		final IDockerConnection connection = getCurrentConnection(activePart);
		if (connection == null || selectedContainers.isEmpty()) {
			return null;
		}
		final Job job = new Job(getJobName(selectedContainers)) {
			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				if (confirmed(selectedContainers)) {
					monitor.beginTask(getJobName(selectedContainers),
							selectedContainers.size());
					for (final IDockerContainer container : selectedContainers) {
						monitor.setTaskName(getTaskName(container));
						executeInJob(container, connection);
						monitor.worked(1);
					}
				}
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		// job.setPriority(Job.LONG);
		job.setUser(true);
		job.schedule();
		return null;
	}

	void openError(final String errorMessage, final Exception e) {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				MessageDialog.openError(Display.getCurrent().getActiveShell(),
						errorMessage, e.getMessage());
			}
		});
	}

	// allow commands to add confirmation dialog
	boolean confirmed(
			@SuppressWarnings("unused") List<IDockerContainer> selectedContainers) {
		return true;
	}

	abstract String getJobName(final List<IDockerContainer> selectedContainers);

	abstract String getTaskName(final IDockerContainer container);

	abstract void executeInJob(final IDockerContainer container,
			final IDockerConnection connection);
}
