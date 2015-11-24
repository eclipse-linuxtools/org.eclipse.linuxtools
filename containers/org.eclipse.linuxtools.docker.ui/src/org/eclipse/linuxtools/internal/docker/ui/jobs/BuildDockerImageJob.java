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

package org.eclipse.linuxtools.internal.docker.ui.jobs;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.DockerImageBuildFailedException;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.docker.core.IDockerProgressHandler;
import org.eclipse.linuxtools.docker.core.IDockerProgressMessage;
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.linuxtools.internal.docker.ui.BuildConsole;
import org.eclipse.linuxtools.internal.docker.ui.launch.IBuildDockerImageLaunchConfigurationConstants;
import org.eclipse.swt.widgets.Display;

/**
 * A {@link Job} to call and progressMonitor the build of an
 * {@link IDockerImage}
 * 
 */
public class BuildDockerImageJob extends Job implements IDockerProgressHandler {

	private static final String BUILD_IMAGE_JOB_TITLE = "BuildImageJog.title"; //$NON-NLS-1$
	private static final String BUILD_IMAGE_ERROR_MESSAGE = "BuildImageError.msg"; //$NON-NLS-1$
	private static final String DOCKERFILE_LINE_COUNT_ERROR = "ImageBuildError.msg"; //$NON-NLS-1$
	private static final String SKIP_EMPTY_DOCKERFILE = "SkipEmptydockerfile.msg"; //$NON-NLS-1$

	/** The {@link IDockerConnection} to use. */
	private final IDockerConnection connection;

	/** The path to the source code. */
	private final IPath path;

	/** the build options. */
	private final Map<String, Object> buildOptions;

	/** the optional repoName (i.e., repo[tag]) for the image to build */
	private final String repoName;

	/** The number of steps to build the image. */
	private final int numberOfBuildOperations;

	/** The console used to display build output messages. */
	private final BuildConsole console;

	/** The progress progressMonitor associated with this {@link Job}. */
	private IProgressMonitor progressMonitor;

	/**
	 * Constructor
	 * 
	 * @param connection
	 *            the Docker connection to use (i.e., on which Docker engine to
	 *            build the image)
	 * @param path
	 *            the path to the source code
	 * @param repoName
	 *            the optional repoName (i.e., repo[tag]) for the image to build
	 * @param buildOptions
	 *            build options
	 * @throws IOException
	 * @see {@link IBuildDockerImageLaunchConfigurationConstants} for build
	 *      options.
	 */
	public BuildDockerImageJob(final IDockerConnection connection,
			final IPath path, final String repoName,
			final Map<String, Object> buildOptions)
					throws DockerException {
		super(JobMessages.getString(BUILD_IMAGE_JOB_TITLE));
		this.connection = connection;
		this.path = path;
		this.repoName = repoName;
		this.buildOptions = buildOptions;
		this.console = BuildConsole.findConsole();
		this.numberOfBuildOperations = countLines(
				path.addTrailingSeparator().append("Dockerfile").toOSString()); //$NON-NLS-1$
	}

	@Override
	protected IStatus run(final IProgressMonitor progressMonitor) {
		try {
			if (numberOfBuildOperations == 0) {
				Activator.log(new Status(IStatus.WARNING, Activator.PLUGIN_ID,
						JobMessages.getString(SKIP_EMPTY_DOCKERFILE)));
			} else {
				this.console.clearConsole();
				this.console.activate();
				this.progressMonitor = progressMonitor;
				this.progressMonitor.beginTask(
						JobMessages.getString(BUILD_IMAGE_JOB_TITLE),
						numberOfBuildOperations + 1);
				if (repoName == null) {
					// Give the Image a default name so it can be tagged later.
					// Otherwise, the Image will be treated as an intermediate
					// Image
					// by the view filters and Tag Image action will be
					// disabled.
					// Use the current time in milliseconds to make it unique.
					final String name = "dockerfile:" //$NON-NLS-1$
							+ Long.toHexString(System.currentTimeMillis());
					connection.buildImage(this.path, name, this,
							this.buildOptions);
				} else {
					connection.buildImage(this.path, this.repoName, this,
							this.buildOptions);
				}
				connection.getImages(true);
			}
		} catch (DockerException | InterruptedException e) {
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					MessageDialog
							.openError(Display.getCurrent().getActiveShell(),
									JobMessages.getString(
											BUILD_IMAGE_ERROR_MESSAGE),
							e.getMessage());
				}

			});
		}
		// make sure the progress monitor is 'done' even if the build failed or
		// timed out.
		this.progressMonitor.done();
		return Status.OK_STATUS;
	}

	@Override
	public void processMessage(final IDockerProgressMessage message)
			throws DockerException {
		if (message.error() != null) {
			cancel();
			throw new DockerImageBuildFailedException(message.error());
		}
		// For imageName build, all the data is in the stream.
		final String status = message.stream();
		if (status != null && status.startsWith("Successfully built")) { //$NON-NLS-1$
			// refresh images
			connection.getImages(true);
		} else if (status != null && status.startsWith("Step")) { //$NON-NLS-1$
			this.progressMonitor.worked(1);
		}
		logMessage(status);
	}

	private void logMessage(final String buildMessage) {
		if (this.console != null) {
			Display.getDefault().asyncExec(new Runnable() {

				@Override
				public void run() {
					console.showConsole();
					try {
						console.write(buildMessage.getBytes("UTF-8")); //$NON-NLS-1$
					} catch (IOException e) {
						Activator.log(e);
					}
				}
			});
		}
	}

	/**
	 * Counts the number of lines in the given Docker build file that contain
	 * statements to execute (ignoring comments and empty lines).
	 * 
	 * @param fileName
	 *            the full repoName of the Docker file to read
	 * @return the number of instructions.
	 * @throws DockerException
	 * @throws IOException
	 */
	private static int countLines(final String fileName)
			throws DockerException {
		try {
			int count = 0;
			try (final InputStream fis = new FileInputStream(fileName);
					final InputStreamReader isr = new InputStreamReader(fis);
					final BufferedReader br = new BufferedReader(isr);) {
				String line;
				while ((line = br.readLine()) != null) {
					// ignore empty lines and comments
					if (line.startsWith("#") || line.trim().isEmpty()) { //$NON-NLS-1$
						continue;
					}
					count++;
				}
			}
			return count;
		} catch (IOException e) {
			throw new DockerException(
					JobMessages.getString(DOCKERFILE_LINE_COUNT_ERROR), e);
		}
	}
}
