/*******************************************************************************
 * Copyright (c) 2017, 2020 Red Hat Inc. and others.
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
package org.eclipse.linuxtools.internal.docker.ui.launch;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerContainerExit;
import org.eclipse.linuxtools.docker.core.IDockerContainerInfo;
import org.eclipse.linuxtools.docker.core.IDockerContainerState;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.linuxtools.internal.docker.ui.jobs.CopyFromDockerJob;
import org.eclipse.linuxtools.internal.docker.ui.jobs.CopyFromDockerJob.CopyType;


public class ContainerCommandProcess extends Process {
	private String containerId;
	private IDockerConnection connection;
	private String imageName;
	private PipedInputStream stdout;
	private PipedInputStream stderr;
	private OutputStream stdin;
	private PipedInputStream pipedStdinIn;
	final private Set<Closeable> toClose = new HashSet<>();
	private Map<String, String> remoteVolumes;
	private boolean keepContainer;
	private Thread thread;
	private Closeable token;
	private boolean containerRemoved;
	private int exitValue;
	private boolean done;
	private boolean threadDone;
	private boolean threadStarted;

	public ContainerCommandProcess(IDockerConnection connection,
			String imageName, String containerId,
			OutputStream outputStream,
			Map<String, String> remoteVolumes,
			boolean keepContainer) {
		this.connection = connection;
		this.imageName = imageName;
		this.containerId = containerId;
		this.remoteVolumes = remoteVolumes;
		this.stdout = new PipedInputStream();
		this.stderr = new PipedInputStream();
		this.keepContainer = keepContainer;
		final IDockerContainerInfo info = connection.getContainerInfo(containerId);
		if (info.config().openStdin()) {
			try {
				PipedOutputStream pipedStdinOut = new PipedOutputStream();
				toClose.add(pipedStdinOut);
				pipedStdinIn = new PipedInputStream(pipedStdinOut);
				toClose.add(pipedStdinIn);
				this.stdin = new OutputStream() {
					@Override
					public void write(int b) throws IOException {
						pipedStdinOut.write(b);
					}
				};
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		} else {
			this.stdin = new ByteArrayOutputStream();
		}
		// Lambda Runnable
		Runnable logContainer = () -> {
			PipedOutputStream pipedOut = null;
			PipedOutputStream pipedErr = null;
			try (PipedOutputStream pipedStdout = new PipedOutputStream(stdout);
					PipedOutputStream pipedStderr = new PipedOutputStream(
							stderr);
					Closeable inputToken = ((DockerConnection) connection).getOperationToken();
					Closeable token = ((DockerConnection) connection)
							.getOperationToken()) {
				this.token = token;
				pipedOut = pipedStdout;
				pipedErr = pipedStderr;
				connection.startContainer(containerId, outputStream);
				threadStarted = true;
				if (info.config().openStdin()) {
					IDockerContainerState state = connection.getContainerInfo(containerId).state();
					do {
						if (!state.running()
								&& (state.finishDate() == null || state.finishDate().before(state.startDate()))) {
							Thread.sleep(50);
						}
						state = info.state();
					} while (!state.running()
							&& (state.finishDate() == null || state.finishDate().before(state.startDate())));
					Thread.sleep(50);
					state = connection.getContainerInfo(containerId).state();
					if (state.running()) {
						((DockerConnection) connection).attachCommand(inputToken, containerId, pipedStdinIn, null,
								false);
						((DockerConnection) connection).attachContainerOutput(token, containerId, pipedStdout,
								pipedStderr);
					}
				} else {
					((DockerConnection) connection).attachLog(token, containerId, pipedStdout, pipedStderr);
				}
				pipedStdout.flush();
				pipedStderr.flush();
			} catch (DockerException | InterruptedException | IOException e) {
				// do nothing but flush/close output streams
				if (pipedOut != null) {
					try {
						pipedOut.flush();
					} catch (IOException e1) {
						// ignore
					}
				}
				if (pipedErr != null) {
					try {
						pipedErr.flush();
					} catch (IOException e1) {
						// ignore
					}
				}
			} catch (Exception e) {
				// do nothing as this will occur if we forcefully stop the attachLog via closing
				// the copy client token
			} finally {
				threadDone = true;
			}
		};

		// start the thread
		this.thread = new Thread(logContainer);
		this.thread.start();
		// ensure we have piped streams set up before allowing exit of
		// constructor
		while (!threadStarted && !threadDone) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// do nothing
			}
		}
	}

	@Override
	public void destroy() {
		try {
			// kill the container
			try {
				connection.killContainer(containerId);
				Thread.sleep(1000);
			} catch (DockerException | InterruptedException e) {
				// ignore
			}
			// give logging thread at most 5 seconds to terminate
			int count = 0;
			while (thread.isAlive() && count++ < 10) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// ignore
				}
			}
			this.stdout.close();
			this.stderr.close();
			this.stdin.close();
			this.token.close();
			for (Closeable close : toClose) {
				close.close();
			}
		} catch (IOException e) {
			// ignore
		}
	}

	@Override
	public int exitValue() {
		// if container has been removed, we need to return
		// the exit value that we cached before removal
		if (containerRemoved) {
			return exitValue;
		}
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
				return state.exitCode().intValue();
			}
		}
		if (containerRemoved) {
			return exitValue;
		}
		return -1;
	}

	@Override
	public synchronized int waitFor() throws InterruptedException {
		if (done) {
			return 0;
		}
		try {
			if (!threadDone) {
				while (!threadStarted) {
					Thread.sleep(200);
				}
			}
			IDockerContainerExit exit = connection
					.waitForContainer(containerId);
			done = true;
			// give logging thread at most 5 seconds to finish
			int i = 0;
			while (!threadDone && i++ < 10) {
				Thread.sleep(500);
			}
			if (!threadDone) {
				// we are stuck
				try {
					this.stdout.close();
					this.stderr.close();
					this.stdin.close();
					this.token.close();
				} catch (IOException e) {
					// do nothing
				}
			}
			if (!containerRemoved) {
				connection.stopLoggingThread(containerId);
			}
			if (!((DockerConnection) connection).isLocal()
					&& remoteVolumes != null && !remoteVolumes.isEmpty()) {
				// public CopyFromImageJob(IDockerConnection connection, CopyType ct, String
				// desc, Set<Path> copySet, Path targetDir,
				// boolean mirror)
				Map<Path, Path> volumes = remoteVolumes.entrySet().stream()
						.collect(Collectors.toMap(x -> new Path(x.getKey()), x -> new Path(x.getValue())));
				final Job job = new CopyFromDockerJob(connection, CopyType.Container, containerId, volumes);
				job.schedule();
				job.join();
				remoteVolumes.clear();
			}
			if (!containerRemoved && !keepContainer) {
				exitValue = exitValue();
				containerRemoved = true;
				connection.removeContainer(containerId);
			}
			return exit.statusCode().intValue();
		} catch (DockerException e) {
			return -1;
		}
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
		return this.stdin;
	}

	public String getImage() {
		return imageName;
	}

}
