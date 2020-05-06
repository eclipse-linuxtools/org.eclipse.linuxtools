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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.linuxtools.docker.core.IDockerIpam;
import org.eclipse.linuxtools.docker.core.IDockerNetwork;
import org.eclipse.linuxtools.docker.core.IDockerNetworkContainer;

import org.mandas.docker.client.messages.Network;

public class DockerNetwork implements IDockerNetwork {

	private String name;
	private String id;
	private String scope;
	private String driver;
	private Map<String, String> options;
	private Map<String, IDockerNetworkContainer> containers;
	private IDockerIpam ipam;

	public DockerNetwork(final Network network) {
		this.name = network.name();
		this.id = network.id();
		this.scope = network.scope();
		this.driver = network.driver();
		this.options = network.options();
		this.containers = new HashMap<>();
		for (String key : network.containers().keySet()) {
			containers.put(key,
					new DockerNetworkContainer(network.containers().get(key)));
		}
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public String id() {
		return id;
	}

	@Override
	public String scope() {
		return scope;
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
	public Map<String, IDockerNetworkContainer> containers() {
		return containers;
	}

	@Override
	public IDockerIpam ipam() {
		return ipam;
	}

}
