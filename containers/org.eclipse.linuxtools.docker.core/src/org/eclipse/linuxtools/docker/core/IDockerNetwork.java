/*******************************************************************************
 * Copyright (c) 2016, 2018 Red Hat.
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
package org.eclipse.linuxtools.docker.core;

import java.util.Map;

/**
 * Docker Network
 */
public interface IDockerNetwork {

	/**
	 * Get name
	 * 
	 * @return name of network
	 */
	String name();

	/**
	 * Get id
	 * 
	 * @return id of network
	 */
	String id();

	/**
	 * Get network scope
	 * 
	 * @return network scope
	 */
	String scope();

	/**
	 * Get driver
	 * 
	 * @return network driver
	 */
	String driver();

	/**
	 * Get options map
	 * 
	 * @return Map of option names to option values
	 */
	Map<String, String> options();

	/**
	 * Get network containers
	 * 
	 * @return Map of container network info
	 */
	Map<String, IDockerNetworkContainer> containers();

	/**
	 * Get IP Address Management info
	 * 
	 * @return ip address management info
	 */
	IDockerIpam ipam();

}
