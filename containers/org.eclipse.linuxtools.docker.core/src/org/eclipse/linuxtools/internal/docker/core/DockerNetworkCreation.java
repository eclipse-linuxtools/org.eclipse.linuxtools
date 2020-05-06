/*******************************************************************************
 * Copyright (c) 2016, 2020 Red Hat.
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

import org.eclipse.linuxtools.docker.core.IDockerNetworkCreation;

import org.mandas.docker.client.messages.NetworkCreation;

public class DockerNetworkCreation implements IDockerNetworkCreation {

	private String id;
	private String warnings;

	public DockerNetworkCreation(NetworkCreation creation) {
		this.id = creation.id();
		this.warnings = creation.warnings();
	}

	@Override
	public String id() {
		return id;
	}

	@Override
	public String warnings() {
		return warnings;
	}

}
