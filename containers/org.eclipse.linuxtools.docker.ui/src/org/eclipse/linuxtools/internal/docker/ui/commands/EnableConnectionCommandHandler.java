/*******************************************************************************
 * Copyright (c) 2016,2018 Red Hat.
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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;

/**
 * Command handler to (re)enable a connection
 */
public class EnableConnectionCommandHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) {
		final IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
		if(activePart instanceof CommonNavigator) {
			final CommonViewer viewer = ((CommonNavigator)activePart).getCommonViewer();
			final ITreeSelection selection = viewer.getStructuredSelection();
			for (TreePath treePath : selection.getPaths()) {
				final IDockerConnection conn = (IDockerConnection) treePath
						.getLastSegment();
				if (!conn.isOpen()) {
					final Job openConnectionJob = new Job(
							CommandMessages.getFormattedString(
									"command.enableconnection", //$NON-NLS-1$
									conn.getUri())) {

						@Override
						protected IStatus run(IProgressMonitor monitor) {
							try {
								conn.open(true);
								Display.getDefault()
										.asyncExec(() -> viewer.refresh(conn));
							} catch (DockerException e) {
								Activator
								.logErrorMessage(
												CommandMessages.getFormattedString("command.enableconnection.failure", //$NON-NLS-1$
														conn.getUri()), 
										e);
								return Status.CANCEL_STATUS;
							}
							return Status.OK_STATUS;
						}
					};
					openConnectionJob.schedule();
				}
			}
		}
		return null;
	}
}
