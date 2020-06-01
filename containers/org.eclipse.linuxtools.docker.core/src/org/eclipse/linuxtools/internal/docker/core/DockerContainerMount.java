/*******************************************************************************
 * Copyright (c) 2020 Red Hat.
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

import org.eclipse.linuxtools.docker.core.IDockerContainerMount;
import org.mandas.docker.client.messages.ContainerMount;

public class DockerContainerMount implements IDockerContainerMount {

	private String name;
	private String source;
	private String destination;
	private String type;
	private String driver;
	private String mode;
	private Boolean rw;
	private String propagation;

	public DockerContainerMount(final ContainerMount mount) {
		this.name = mount.name();
		this.source = mount.source();
		this.destination = mount.destination();
		this.type = mount.type();
		this.driver = mount.driver();
		this.mode = mount.mode();
		this.rw = mount.rw();
		this.propagation = mount.propagation();
	}

	@Override
	public String type() {
		return type;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public String source() {
		return source;
	}

	@Override
	public String destination() {
		return destination;
	}

	@Override
	public String driver() {
		return driver;
	}

	@Override
	public String mode() {
		return mode;
	}

	@Override
	public Boolean rw() {
		return rw;
	}

	@Override
	public String propagation() {
		return propagation;
	}

}
