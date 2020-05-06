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

import java.util.Map;

import org.eclipse.linuxtools.docker.core.IDockerIpam;
import org.eclipse.linuxtools.docker.core.IDockerNetworkConfig;

import org.mandas.docker.client.messages.NetworkConfig;

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
