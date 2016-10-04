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
package org.eclipse.linuxtools.internal.docker.ui.commands;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerContainer;
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.linuxtools.docker.ui.wizards.ImageSearch;
import org.eclipse.linuxtools.internal.docker.core.ContainerFileProxy;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.linuxtools.internal.docker.ui.TarEntry;
import org.eclipse.linuxtools.internal.docker.ui.TarException;
import org.eclipse.linuxtools.internal.docker.ui.TarInputStream;
import org.eclipse.linuxtools.internal.docker.ui.wizards.ContainerCopyFrom;
import org.eclipse.swt.widgets.Display;
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
	private static final String ERROR_COPYING_FROM_CONTAINER = "command.copyfromcontainer.error.msg"; //$NON-NLS-1$
	private static final String COPY_FROM_CONTAINER_JOB_TASK = "command.copyfromcontainer.job.task"; //$NON-NLS-1$
	private static final String COPY_FROM_CONTAINER_JOB_TITLE = "command.copyfromcontainer.job.title"; //$NON-NLS-1$
	private static final String COPY_FROM_CONTAINER_JOB_SUBTASK = "command.copyfromcontainer.job.subtask"; //$NON-NLS-1$

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

	private void performCopyFromContainer(final IDockerConnection connection,
			final IDockerContainer container, final String target,
			final List<ContainerFileProxy> files) {
		final Job copyFromContainerJob = new Job(
				CommandMessages.getFormattedString(
				COPY_FROM_CONTAINER_JOB_TITLE, container.name())) {

			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				monitor.beginTask(
						CommandMessages.getString(COPY_FROM_CONTAINER_JOB_TASK),
						files.size());
				try {
					for (ContainerFileProxy proxy : files) {
						if (monitor.isCanceled()) {
							monitor.done();
							return Status.CANCEL_STATUS;
						}
						try {
							monitor.setTaskName(
									CommandMessages.getFormattedString(
											COPY_FROM_CONTAINER_JOB_SUBTASK,
											proxy.getFullPath()));
							monitor.worked(1);
							TarInputStream k = new TarInputStream(
									((DockerConnection) connection)
											.copyContainer(container.id(),
													proxy.getLink()));
							TarEntry te = k.getNextEntry();
							long size = te.getSize();
							IPath path = new Path(target);
							path = path.append(proxy.getName());
							File f = new File(path.toOSString());
							f.createNewFile();
							FileOutputStream os = new FileOutputStream(f);
							if (size > 4096)
								size = 4096;
							byte[] barray = new byte[(int) size];
							while (k.read(barray) > 0) {
								if (monitor.isCanceled()) {
									monitor.done();
									k.close();
									os.close();
									return Status.CANCEL_STATUS;
								}
								os.write(barray);
							}
							k.close();
							os.close();
						} catch (final DockerException e) {
							Display.getDefault()
									.syncExec(() -> MessageDialog.openError(
											PlatformUI.getWorkbench()
													.getActiveWorkbenchWindow()
													.getShell(),
											CommandMessages.getFormattedString(
													ERROR_COPYING_FROM_CONTAINER,
													proxy.getLink(),
													container.name()),
											e.getMessage()));
							// for now
						}
					}
				} catch (InterruptedException e) {
					// do nothing
				} catch (TarException | IOException e) {
					Activator.log(e);
				} finally {
					monitor.done();
				}
				return Status.OK_STATUS;
			}

		};

		copyFromContainerJob.schedule();

	}

}
