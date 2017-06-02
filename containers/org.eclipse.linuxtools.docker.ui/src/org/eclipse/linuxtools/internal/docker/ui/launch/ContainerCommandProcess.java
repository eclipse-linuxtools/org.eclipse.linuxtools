/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.ui.launch;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Map;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerContainerConfig;
import org.eclipse.linuxtools.docker.core.IDockerContainerExit;
import org.eclipse.linuxtools.docker.core.IDockerContainerInfo;
import org.eclipse.linuxtools.docker.core.IDockerContainerState;
import org.eclipse.linuxtools.docker.core.IDockerHostConfig;
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.linuxtools.docker.ui.launch.Messages;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.linuxtools.internal.docker.core.DockerContainerConfig;
import org.eclipse.linuxtools.internal.docker.core.DockerHostConfig;

public class ContainerCommandProcess extends Process {

	private String containerId;
	private IDockerConnection connection;
	private String imageName;
	private PipedInputStream stdout;
	private PipedInputStream stderr;
	private Map<String, String> remoteVolumes;
	private boolean keepContainer;
	private Thread thread;

	public ContainerCommandProcess(IDockerConnection connection,
			String imageName, String containerId,
			Map<String, String> remoteVolumes,
			boolean keepContainer) {
		this.connection = connection;
		this.imageName = imageName;
		this.containerId = containerId;
		this.remoteVolumes = remoteVolumes;
		this.stdout = new PipedInputStream();
		this.stderr = new PipedInputStream();
		this.keepContainer = keepContainer;
		// Lambda Runnable
		Runnable logContainer = () -> {
			try (PipedOutputStream pipedStdout = new PipedOutputStream(stdout);
					PipedOutputStream pipedStderr = new PipedOutputStream(
							stderr)) {
				connection.attachLog(containerId, pipedStdout, pipedStderr);
				pipedStdout.flush();
				pipedStderr.flush();
			} catch (DockerException | InterruptedException | IOException e) {
				// do nothing but close output streams
			}
		};

		// start the thread
		this.thread = new Thread(logContainer);
		this.thread.start();
	}

	@Override
	public void destroy() {
		try {
			try {
				// TODO: see if there is a better way of draining the
				// container output before closing the streams. Note
				// that trying to join the attachLog thread does not
				// work.
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				// ignore
			}
			this.stdout.close();
			this.stderr.close();
		} catch (IOException e) {
			// ignore
		}
		thread.interrupt();
	}

	@Override
	public int exitValue() {
		IDockerContainerInfo info = connection
				.getContainerInfo(containerId);
		if (info != null) {
			IDockerContainerState state = info.state();
			if (state != null) {
				if (state.paused() || state.restarting() || state.running()) {
					throw new IllegalThreadStateException(
							LaunchMessages.getFormattedString(
									"ContainerNotFinished.msg", containerId)); //$NON-NLS-1$
				}
				return state.exitCode();
			}
		}
		return -1;
	}

	@Override
	public InputStream getErrorStream() {
		return stderr;
	}

	@Override
	public InputStream getInputStream() {
		return stdout;
	}

	@Override
	public OutputStream getOutputStream() {
		return new ByteArrayOutputStream();
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

	private class CopyVolumesFromImageJob extends Job {

		private static final String COPY_VOLUMES_FROM_JOB_TITLE = "ContainerLaunch.copyVolumesFromJob.title"; //$NON-NLS-1$
		private static final String COPY_VOLUMES_FROM_DESC = "ContainerLaunch.copyVolumesFromJob.desc"; //$NON-NLS-1$
		private static final String COPY_VOLUMES_FROM_TASK = "ContainerLaunch.copyVolumesFromJob.task"; //$NON-NLS-1$

		private final Map<String, String> remoteVolumes;
		private final IDockerConnection connection;
		private final String imageName;

		public CopyVolumesFromImageJob(IDockerConnection connection,
				String imageName, Map<String, String> remoteVolumes) {
			super(Messages.getString(COPY_VOLUMES_FROM_JOB_TITLE));
			this.remoteVolumes = remoteVolumes;
			this.connection = connection;
			this.imageName = imageName;
		}

		@Override
		protected IStatus run(final IProgressMonitor monitor) {
			monitor.beginTask(Messages.getFormattedString(
					COPY_VOLUMES_FROM_DESC, imageName), remoteVolumes.size());
			String containerId = null;
			try {
				DockerContainerConfig.Builder builder = new DockerContainerConfig.Builder()
						.cmd("/bin/sh").image(imageName); //$NON-NLS-1$
				IDockerContainerConfig config = builder.build();
				DockerHostConfig.Builder hostBuilder = new DockerHostConfig.Builder();
				IDockerHostConfig hostConfig = hostBuilder.build();
				containerId = ((DockerConnection) connection)
						.createContainer(config, hostConfig, null);
				for (String volume : remoteVolumes.keySet()) {
					try {
						monitor.setTaskName(Messages.getFormattedString(
								COPY_VOLUMES_FROM_TASK, volume));
						monitor.worked(1);

						InputStream in = ((DockerConnection) connection)
								.copyContainer(containerId,
										remoteVolumes.get(volume));

						/*
						 * The input stream from copyContainer might be
						 * incomplete or non-blocking so we should wrap it in a
						 * stream that is guaranteed to block until data is
						 * available.
						 */
						TarArchiveInputStream k = new TarArchiveInputStream(
								new BlockingInputStream(in));
						TarArchiveEntry te = null;
						IPath currDir = new Path(volume).removeLastSegments(1);
						currDir.toFile().mkdirs();
						while ((te = k.getNextTarEntry()) != null) {
							long size = te.getSize();
							IPath path = currDir;
							path = path.append(te.getName());
							File f = new File(path.toOSString());
							if (te.isDirectory()) {
								f.mkdir();
								continue;
							} else {
								f.createNewFile();
							}
							FileOutputStream os = new FileOutputStream(f);
							int bufferSize = ((int) size > 4096 ? 4096
									: (int) size);
							byte[] barray = new byte[bufferSize];
							int result = -1;
							while ((result = k.read(barray, 0,
									bufferSize)) > -1) {
								if (monitor.isCanceled()) {
									monitor.done();
									k.close();
									os.close();
									return Status.CANCEL_STATUS;
								}
								os.write(barray, 0, result);
							}
							os.close();
						}
						k.close();
					} catch (final DockerException e) {
						// ignore
					}
				}
			} catch (InterruptedException e) {
				// do nothing
			} catch (IOException e) {
				Activator.log(e);
			} catch (DockerException e1) {
				Activator.log(e1);
			} finally {
				if (containerId != null) {
					try {
						((DockerConnection) connection)
								.removeContainer(containerId);
					} catch (DockerException | InterruptedException e) {
						// ignore
					}
				}
				monitor.done();
			}
			return Status.OK_STATUS;

		}
	}

	@Override
	public int waitFor() throws InterruptedException {
		try {
			IDockerContainerExit exit = connection
					.waitForContainer(containerId);
			connection.stopLoggingThread(containerId);
			if (!keepContainer) {
				connection.removeContainer(containerId);
			}
			if (!((DockerConnection) connection).isLocal()) {
				CopyVolumesFromImageJob job = new CopyVolumesFromImageJob(
						connection, imageName, remoteVolumes);
				job.schedule();
				job.join();
			}
			return exit.statusCode();
		} catch (DockerException e) {
			return -1;
		}
	}

	public String getImage() {
		return imageName;
	}

}
