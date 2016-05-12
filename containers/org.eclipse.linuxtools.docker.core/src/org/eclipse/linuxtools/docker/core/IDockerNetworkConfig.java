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
	public String name();

	/**
	 * Get network driver
	 * 
	 * @return network driver
	 */
	public String driver();

	/**
	 * Get IP address management info
	 * 
	 * @return ip address management info
	 */
	public IDockerIpam ipam();

	/**
	 * Get network options
	 * 
	 * @return Map of option names to option values
	 */
	public Map<String, String> options();

}
