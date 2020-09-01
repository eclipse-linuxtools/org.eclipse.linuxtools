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

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
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

	/**
	 * A blocking input stream that waits until data is available.
	 */
	private class BlockingInputStream extends InputStream {
		private InputStream in;

		public BlockingInputStream(InputStream in) {
			this.in = in;
		}

		@Override
		public int read() throws IOException {
			return in.read();
		}
	}

	private static Set<PosixFilePermission> toPerms(int mode) {
		Set<PosixFilePermission> perms = new HashSet<>();
		if ((mode & 0400) != 0) {
			perms.add(PosixFilePermission.OWNER_READ);
		}
		if ((mode & 0200) != 0) {
			perms.add(PosixFilePermission.OWNER_WRITE);
		}
		if ((mode & 0100) != 0) {
			perms.add(PosixFilePermission.OWNER_EXECUTE);
		}
		if ((mode & 0040) != 0) {
			perms.add(PosixFilePermission.GROUP_READ);
		}
		if ((mode & 0020) != 0) {
			perms.add(PosixFilePermission.GROUP_WRITE);
		}
		if ((mode & 0010) != 0) {
			perms.add(PosixFilePermission.GROUP_EXECUTE);
		}
		if ((mode & 0004) != 0) {
			perms.add(PosixFilePermission.OTHERS_READ);
		}
		if ((mode & 0002) != 0) {
			perms.add(PosixFilePermission.OTHERS_WRITE);
		}
		if ((mode & 0001) != 0) {
			perms.add(PosixFilePermission.OTHERS_EXECUTE);
		}
		return perms;
	}

	private void performCopyFromContainer(final IDockerConnection connection, final IDockerContainer container,
			final String target, final List<ContainerFileProxy> files) {
		final Job copyFromContainerJob = new Job(
				CommandMessages.getFormattedString(COPY_FROM_CONTAINER_JOB_TITLE, container.name())) {

			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				monitor.beginTask(CommandMessages.getString(COPY_FROM_CONTAINER_JOB_TASK), files.size());
				boolean isWin = Platform.getOS().equals(Platform.OS_WIN32);
				try (Closeable token = ((DockerConnection) connection).getOperationToken()) {
					Map<String, String> links = new HashMap<>();
					for (ContainerFileProxy proxy : files) {
						if (monitor.isCanceled()) {
							monitor.done();
							return Status.CANCEL_STATUS;
						}
						try {
							monitor.setTaskName(CommandMessages.getFormattedString(COPY_FROM_CONTAINER_JOB_SUBTASK,
									proxy.getFullPath()));
							monitor.worked(1);
							InputStream in = ((DockerConnection) connection).copyContainer(token, container.id(),
									proxy.getLink());
							/*
							 * The input stream from copyContainer might be incomplete or non-blocking so we
							 * should wrap it in a stream that is guaranteed to block until data is
							 * available.
							 */
							try (TarArchiveInputStream k = new TarArchiveInputStream(new BlockingInputStream(in))) {
								TarArchiveEntry te = null;
								while ((te = k.getNextTarEntry()) != null) {
									long size = te.getSize();
									IPath path = new Path(target);
									path = path.append(te.getName());
									File f = new File(path.toOSString());
									int mode = te.getMode();
									if (te.isDirectory()) {
										f.mkdir();
										if (!isWin && !te.isSymbolicLink()) {
											Files.setPosixFilePermissions(Paths.get(path.toOSString()), toPerms(mode));
										}
										continue;
									} else {
										f.createNewFile();
										if (!isWin && !te.isSymbolicLink()) {
											Files.setPosixFilePermissions(Paths.get(path.toOSString()), toPerms(mode));
										}
									}
									try (FileOutputStream os = new FileOutputStream(f)) {
										int bufferSize = ((int) size > 4096 ? 4096 : (int) size);
										byte[] barray = new byte[bufferSize];
										int result = -1;
										if (size == 0 && te.isSymbolicLink()) {
											IPath linkPath = new Path(te.getLinkName());
											if (!linkPath.isAbsolute()) {
												if (proxy.isFolder()) {
													linkPath = new Path(proxy.getFullPath()).append(te.getLinkName());
												} else {
													linkPath = new Path(proxy.getFullPath()).removeLastSegments(1)
															.append(te.getLinkName());
												}

											}
											links.put(te.getName(), linkPath.toPortableString());
										} else {
											while ((result = k.read(barray, 0, bufferSize)) > -1) {
												if (monitor.isCanceled()) {
													monitor.done();
													k.close();
													os.close();
													return Status.CANCEL_STATUS;
												}
												os.write(barray, 0, result);
											}
										}
									}
								}
							}
						} catch (final DockerException e) {
							Display.getDefault()
									.syncExec(() -> MessageDialog.openError(
											PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
											CommandMessages.getFormattedString(ERROR_COPYING_FROM_CONTAINER,
													proxy.getLink(), container.name()),
											e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
						}
					}
					for (String name : links.keySet()) {
						String link = links.get(name);
						try {
							InputStream in = ((DockerConnection) connection).copyContainer(token, container.id(),
									link);
							/*
							 * The input stream from copyContainer might be incomplete or non-blocking so we
							 * should wrap it in a stream that is guaranteed to block until data is
							 * available.
							 */
							try (TarArchiveInputStream k = new TarArchiveInputStream(new BlockingInputStream(in))) {
								TarArchiveEntry te = k.getNextTarEntry();
								if (te != null) {
									long size = te.getSize();
									IPath path = new Path(target);
									path = path.append(name);
									File f = new File(path.toOSString());
									f.createNewFile();
									try (FileOutputStream os = new FileOutputStream(f)) {
										int bufferSize = ((int) size > 4096 ? 4096 : (int) size);
										byte[] barray = new byte[bufferSize];
										int result = -1;
										while ((result = k.read(barray, 0, bufferSize)) > -1) {
											if (monitor.isCanceled()) {
												monitor.done();
												k.close();
												os.close();
												return Status.CANCEL_STATUS;
											}
											os.write(barray, 0, result);
										}
									}
								}
							}
						} catch (final DockerException e) {
							Display.getDefault()
									.syncExec(() -> MessageDialog.openError(
											PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
											CommandMessages.getFormattedString(ERROR_COPYING_FROM_CONTAINER,
													name, container.name()),
											e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
						}
					}
				} catch (InterruptedException e) {
					// do nothing
				} catch (IOException e) {
					Activator.log(e);
				} catch (DockerException e1) {
					Activator.log(e1);
				} finally {
					monitor.done();
				}
				return Status.OK_STATUS;
			}

		};

		copyFromContainerJob.schedule();
	}

}
