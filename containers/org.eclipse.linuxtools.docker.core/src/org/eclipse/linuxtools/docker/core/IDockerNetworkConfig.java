/*******************************************************************************
 * Copyright (c) 2018 Red Hat.
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
 * Docker Network Configuration
 * 
 * Needed to create a Docker Network
 * 
 * @author jjohnstn
 *
 */
public interface IDockerNetworkConfig {

	/**
	 * Get name of network
	 * 
	 * @return name of network
	 */
	String name();

	/**
	 * Get network driver
	 * 
	 * @return network driver
	 */
	String driver();

	/**
	 * Get IP address management info
	 * 
	 * @return ip address management info
	 */
	IDockerIpam ipam();

	/**
	 * Get network options
	 * 
	 * @return Map of option names to option values
	 */
	Map<String, String> options();

}
