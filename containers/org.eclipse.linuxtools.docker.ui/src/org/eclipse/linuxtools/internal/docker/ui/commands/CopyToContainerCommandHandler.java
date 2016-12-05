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
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
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
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.linuxtools.docker.ui.wizards.ImageSearch;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.linuxtools.internal.docker.ui.wizards.ContainerCopyTo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Command handler that opens the {@link ImageSearch} wizard and pulls the
 * selected image in background on completion.
 *
 */
public class CopyToContainerCommandHandler extends AbstractHandler {

	private static final String ERROR_COPYING_TO_CONTAINER_NO_CONNECTION = "command.copytocontainer.failure.no_connection"; //$NON-NLS-1$
	private static final String MISSING_CONNECTION = "missing_connection"; //$NON-NLS-1$
	private static final String ERROR_COPYING_TO_CONTAINER = "command.copytocontainer.error.msg"; //$NON-NLS-1$
	private static final String COPY_TO_CONTAINER_JOB_TASK = "command.copytocontainer.job.task"; //$NON-NLS-1$
	private static final String COPY_TO_CONTAINER_JOB_TITLE = "command.copytocontainer.job.title"; //$NON-NLS-1$
	private static final String COPY_TO_CONTAINER_JOB_SUBTASK = "command.copytocontainer.job.subtask"; //$NON-NLS-1$

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
									ERROR_COPYING_TO_CONTAINER_NO_CONNECTION));
		} else {
			final ContainerCopyTo wizard = new ContainerCopyTo(connection,
					container);
			final boolean copyToContainer = CommandUtils.openWizard(wizard,
					HandlerUtil.getActiveShell(event));
			if (copyToContainer) {
				performCopyToContainer(connection, container,
						wizard.getTarget(), wizard.getSources());
			}
		}
		return null;
	}

	private void performCopyToContainer(final IDockerConnection connection,
			final IDockerContainer container, final String target,
			final List<Object> files) {
		final Job copyToContainerJob = new Job(
				CommandMessages.getFormattedString(
						COPY_TO_CONTAINER_JOB_TITLE, container.name())) {

			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				monitor.beginTask(
						CommandMessages.getString(COPY_TO_CONTAINER_JOB_TASK),
						files.size() + 1);
				java.nio.file.Path tmpDir = null;
				try {
					for (Object proxy : files) {
						File file = (File) proxy;
						if (monitor.isCanceled()) {
							monitor.done();
							return Status.CANCEL_STATUS;
						}
						try {
							monitor.setTaskName(
									CommandMessages.getFormattedString(
											COPY_TO_CONTAINER_JOB_SUBTASK,
											proxy.toString()));
							monitor.worked(1);
							// we can only copy from a directory so copy the
							// files to a temporary directory
							if (tmpDir == null) {
								tmpDir = Files.createTempDirectory(
										Activator.PLUGIN_ID);
							}
							if (file.isDirectory()) {
								java.nio.file.Path sourcePath = FileSystems
										.getDefault()
										.getPath(file.getAbsolutePath());
								final java.nio.file.Path target = tmpDir
										.resolve(sourcePath.getFileName());
								Files.createDirectory(target);
								Files.walkFileTree(sourcePath,
										new SimpleFileVisitor<Path>() {
											@Override
											public FileVisitResult preVisitDirectory(
													final Path dir,
													final BasicFileAttributes attrs)
													throws IOException {
												java.nio.file.Path targetPath = target
														.resolve(sourcePath
																.relativize(
																		dir));
												Files.createDirectories(
														targetPath);
												return FileVisitResult.CONTINUE;
											}

											@Override
											public FileVisitResult visitFile(
													final Path file,
													final BasicFileAttributes attrs)
													throws IOException {
												monitor.setTaskName(
														CommandMessages
																.getFormattedString(
																		COPY_TO_CONTAINER_JOB_SUBTASK,
																		file.toString()));

												Files.copy(file, target.resolve(
														sourcePath.relativize(
																file)));
												return FileVisitResult.CONTINUE;
											}
										});


							} else {
								monitor.worked(1);
								java.nio.file.Path sourcePath = FileSystems
										.getDefault()
										.getPath(file.getAbsolutePath());
								java.nio.file.Path targetPath = tmpDir
										.resolve(sourcePath.getFileName());
								Files.copy(sourcePath,
										targetPath,
										StandardCopyOption.REPLACE_EXISTING);
							}
						} catch (final IOException e) {
							Display.getDefault()
									.syncExec(() -> MessageDialog.openError(
											PlatformUI.getWorkbench()
													.getActiveWorkbenchWindow()
													.getShell(),
											CommandMessages.getFormattedString(
													ERROR_COPYING_TO_CONTAINER,
													proxy.toString(),
													container.name()),
											e.getMessage()));
						}
					}
					// copy any individual files picked to Container
					// via the temporary directory
					try {
						((DockerConnection) connection).copyToContainer(
								tmpDir.toString(), container.id(), target);
						deleteTmpDir(tmpDir);
					} catch (final DockerException | IOException e) {
						Display.getDefault()
								.syncExec(() -> MessageDialog.openError(
										PlatformUI.getWorkbench()
												.getActiveWorkbenchWindow()
												.getShell(),
										CommandMessages.getFormattedString(
												ERROR_COPYING_TO_CONTAINER,
												target, container.name()),
										e.getCause() != null
												? e.getCause().getMessage()
												: e.getMessage()));

					}
				} catch (InterruptedException e) {
					// do nothing
				} finally {
					monitor.done();
				}
				return Status.OK_STATUS;
			}

		};

		copyToContainerJob.schedule();

	}

	private void deleteTmpDir(java.nio.file.Path tmpPath) {
		File tmpDir = tmpPath.toFile();
		for (File f : tmpDir.listFiles()) {
			f.delete();
		}
		tmpDir.delete();
	}

}
