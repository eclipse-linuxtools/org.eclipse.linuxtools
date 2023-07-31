/********************************************************************************
 * Copyright (c) 2021, 2023 Eclipse Linux Tools project committers and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 ********************************************************************************/

package org.eclipse.linuxtools.internal.docker.core;

import java.util.List;

import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerContainerConfig;
import org.eclipse.linuxtools.docker.core.IDockerHostConfig;

/**
 * This creates a container that implements AutoColosable. Thus allowing to use
 * it in a try statement. Special care is taken that the container is removed
 * eventually (currently 3600s), even if eclipse crashes.
 */
public class CloseableContainer implements AutoCloseable {

	public final String containerId;
	private final IDockerConnection connection;

	/**
	 * Create a closable container, that is removed after leaving the try-block.
	 *
	 * @param connection
	 *            The docker-connection to use
	 * @param image
	 *            The image name to use
	 * @throws DockerException
	 *             if an docker related error occurs
	 * @throws InterruptedException
	 *             if the thread was interrupted
	 */
	public CloseableContainer(IDockerConnection connection, String image)
			throws DockerException, InterruptedException {
		// Create base container to use for copying
		// It must be running, to execute ls & co

		this.connection = connection;
		DockerContainerConfig.Builder builder = new DockerContainerConfig.Builder().cmd("sleep 3600") //$NON-NLS-1$
				.image(image);
		IDockerContainerConfig config = builder.build();
		DockerHostConfig.Builder hostBuilder = new DockerHostConfig.Builder();
		// Remove the container after usage.
		hostBuilder.autoRemove(true);
		// seccomp=unconfined
		hostBuilder.securityOpt(List.of("seccomp=unconfined")); //$NON-NLS-1$
		IDockerHostConfig hostConfig = hostBuilder.build();
		containerId = connection.createContainer(config, hostConfig, null);

	}

	/**
	 * Start the Container
	 *
	 * @throws DockerException
	 *             if an docker related error occurs
	 * @throws InterruptedException
	 *             if the thread was interrupted
	 */
	public void start() throws DockerException, InterruptedException {
		connection.startContainer(containerId, null);
	}

	/**
	 * Close the container, by killing it. Remove it afterwards, just in case.
	 * setting it to autoremove in the constructor should do the job, though.
	 */
	@Override
	public void close() throws DockerException, InterruptedException {
		try {
			connection.killContainer(containerId);
		} catch (Exception e) {
		}
		connection.removeContainer(containerId);
	}

}
