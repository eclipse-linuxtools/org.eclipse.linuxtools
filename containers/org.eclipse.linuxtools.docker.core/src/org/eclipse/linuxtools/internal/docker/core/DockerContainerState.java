/*******************************************************************************
 * Copyright (c) 2014 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.core;

import java.util.Date;

import org.eclipse.linuxtools.docker.core.IDockerContainerState;

import com.spotify.docker.client.messages.ContainerState;

public class DockerContainerState implements IDockerContainerState {

	private final Integer pid;
	private final Boolean running;
	private final Boolean paused;
	private final Boolean restarting;
	private final Integer exitCode;
	private final Date startDate;
	private final Date finishDate;

	public DockerContainerState(final ContainerState containerState) {
		this.pid = containerState.pid();
		this.running = containerState.running();
		this.paused = containerState.paused();
		this.restarting = containerState.restarting();
		this.exitCode = containerState.exitCode();
		this.startDate = containerState.startedAt();
		this.finishDate = containerState.finishedAt();
	}

	@Override
	public Boolean running() {
		return running;
	}

	@Override
	public Boolean restarting() {
		return restarting;
	}
	
	@Override
	public Boolean paused() {
		return paused;
	}
	
	@Override
	public Integer pid() {
		return pid;
	}

	@Override
	public Integer exitCode() {
		return exitCode;
	}

	@Override
	public Date startDate() {
		return startDate;
	}

	@Override
	public Date finishDate() {
		return finishDate;
	}

}
