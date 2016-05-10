/*******************************************************************************
 * Copyright (c) 2016 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.core;

import org.eclipse.linuxtools.docker.core.IDockerNetworkCreation;

import com.spotify.docker.client.messages.NetworkCreation;

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
