/*******************************************************************************
 * Copyright (c) 2019, 2020 Red Hat.
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

import java.util.Map;

import org.eclipse.linuxtools.docker.core.IDockerVolume;

import org.mandas.docker.client.messages.Volume;

public class DockerVolume implements IDockerVolume {

	private String name;
	private String driver;
	private Map<String, String> driverOpts;
	private String mountPoint;
	private String scope;
	private Map<String, String> options;
	private Map<String, String> labels;
	private Map<String, String> status;

	public DockerVolume(final Volume volume) {
		this.name = volume.name();
		this.driver = volume.driver();
		this.driverOpts = volume.driverOpts();
		this.mountPoint = volume.mountpoint();
		this.scope = volume.scope();
		this.options = volume.options();
		this.labels = volume.labels();
		this.status = volume.status();
	}


	@Override
	public String name() {
		return name;
	}

	@Override
	public String driver() {
		return driver;
	}

	@Override
	public Map<String, String> options() {
		return options;
	}

	@Override
	public Map<String, String> labels() {
		return labels;
	}

	@Override
	public String mountPoint() {
		return mountPoint;
	}

	@Override
	public String scope() {
		return scope;
	}

	@Override
	public Map<String, String> status() {
		return status;
	}

	@Override
	public Map<String, String> driverOpts() {
		return driverOpts;
	}

}
