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

import java.util.Map;

import org.eclipse.linuxtools.docker.core.IDockerIpam;
import org.eclipse.linuxtools.docker.core.IDockerNetworkConfig;

import com.spotify.docker.client.messages.NetworkConfig;

public class DockerNetworkConfig implements IDockerNetworkConfig {

	private String name;
	private String driver;
	private IDockerIpam ipam;
	private Map<String, String> options;

	public DockerNetworkConfig(final NetworkConfig cfg) {
		this.name = cfg.name();
		this.driver = cfg.driver();
		this.options = cfg.options();
		this.ipam = new DockerIpam(cfg.ipam());
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
	public IDockerIpam ipam() {
		return ipam;
	}

	@Override
	public Map<String, String> options() {
		return options;
	}

}
