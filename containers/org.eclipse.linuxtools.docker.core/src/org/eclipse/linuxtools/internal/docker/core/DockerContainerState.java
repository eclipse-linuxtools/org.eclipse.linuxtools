/*******************************************************************************
 * Copyright (c) 2014, 2020 Red Hat.
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
package org.eclipse.linuxtools.internal.docker.core;

import java.util.Date;

import org.eclipse.linuxtools.docker.core.IDockerContainerState;
import org.mandas.docker.client.messages.ContainerState;

public class DockerContainerState implements IDockerContainerState {

	private final Integer pid;
	private final Boolean running;
	private final Boolean paused;
	private final Boolean restarting;
	private final Long exitCode;
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
		return Integer.valueOf(exitCode.intValue());
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
