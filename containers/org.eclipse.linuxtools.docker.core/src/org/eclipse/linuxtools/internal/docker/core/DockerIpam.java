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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.linuxtools.docker.core.IDockerIpam;
import org.eclipse.linuxtools.docker.core.IDockerIpamConfig;

import com.spotify.docker.client.messages.Ipam;
import com.spotify.docker.client.messages.IpamConfig;

public class DockerIpam implements IDockerIpam {

	private String driver;
	private List<IDockerIpamConfig> configs;

	public DockerIpam(final Ipam ipam) {
		this.driver = ipam.driver();
		this.configs = new ArrayList<>();
		for (IpamConfig cfg : ipam.config()) {
			this.configs.add(new DockerIpamConfig(cfg));
		}
	}

	@Override
	public String driver() {
		return driver;
	}

	@Override
	public List<IDockerIpamConfig> config() {
		return configs;
	}

}
