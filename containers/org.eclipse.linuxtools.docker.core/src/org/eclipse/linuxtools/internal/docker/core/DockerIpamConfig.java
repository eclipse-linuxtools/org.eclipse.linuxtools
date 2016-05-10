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

import org.eclipse.linuxtools.docker.core.IDockerIpamConfig;

import com.spotify.docker.client.messages.IpamConfig;

public class DockerIpamConfig implements IDockerIpamConfig {

	private String subnet;
	private String ipRange;
	private String gateway;

	public DockerIpamConfig(final IpamConfig cfg) {
		this.subnet = cfg.subnet();
		this.ipRange = cfg.ipRange();
		this.gateway = cfg.gateway();
	}

	@Override
	public String subnet() {
		return subnet;
	}

	@Override
	public void subnet(String subnet) {
		this.subnet = subnet;

	}

	@Override
	public String ipRange() {
		return ipRange;
	}

	@Override
	public void ipRange(String ipRange) {
		this.ipRange = ipRange;
	}

	@Override
	public String gateway() {
		return gateway;
	}

	@Override
	public void gateway(String gateway) {
		this.gateway = gateway;
	}

}
