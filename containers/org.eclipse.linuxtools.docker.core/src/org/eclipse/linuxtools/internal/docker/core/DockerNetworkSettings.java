/*******************************************************************************
 * Copyright (c) 2014, 2015 Red Hat.
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.linuxtools.docker.core.IDockerNetworkSettings;
import org.eclipse.linuxtools.docker.core.IDockerPortBinding;

import com.spotify.docker.client.messages.NetworkSettings;
import com.spotify.docker.client.messages.PortBinding;

/**
 * @author xcoulon
 *
 */
public class DockerNetworkSettings implements IDockerNetworkSettings {

	private final String bridge;
	private final String gateway;
	private final String ipAddress;
	private final Integer ipPrefixLen;
	private final Map<String, Map<String, String>> portMapping;
	private final Map<String, List<IDockerPortBinding>> ports;

	public DockerNetworkSettings(final NetworkSettings networkSettings) {
		this.bridge = networkSettings.bridge();
		this.gateway = networkSettings.gateway();
		this.ipAddress = networkSettings.ipAddress();
		this.ipPrefixLen = networkSettings.ipPrefixLen();
		this.portMapping = networkSettings.portMapping();
		this.ports = new HashMap<>();
		if(networkSettings.ports() != null) {
			for(Entry<String, List<PortBinding>> entry : networkSettings.ports().entrySet()) {
				final List<IDockerPortBinding> values = new ArrayList<>();
				if(entry.getValue() != null) {
					for(PortBinding portBinding : entry.getValue()) {
						values.add(new DockerPortBinding(portBinding));
					}
				}
				ports.put(entry.getKey(), values);
			}
			
		}
	}

	/**
	 * @return the bridge
	 */
	@Override
	public String bridge() {
		return bridge;
	}

	/**
	 * @return the gateway
	 */
	@Override
	public String gateway() {
		return gateway;
	}

	/**
	 * @return the ipAddress
	 */
	@Override
	public String ipAddress() {
		return ipAddress;
	}

	/**
	 * @return the ipPrefixLen
	 */
	@Override
	public Integer ipPrefixLen() {
		return ipPrefixLen;
	}

	/**
	 * @return the portMapping
	 */
	@Override
	public Map<String, Map<String, String>> portMapping() {
		return portMapping;
	}

	/**
	 * @return the ports
	 */
	@Override
	public Map<String, List<IDockerPortBinding>> ports() {
		return ports;
	}
	
}
