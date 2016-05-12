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
 * Docker Network
 * 
 * @author jjohnstn
 *
 */
public interface IDockerNetwork {

	/**
	 * Get name
	 * 
	 * @return name of network
	 */
	public String name();

	/**
	 * Get id
	 * 
	 * @return id of network
	 */
	public String id();

	/**
	 * Get network scope
	 * 
	 * @return network scope
	 */
	public String scope();

	/**
	 * Get driver
	 * 
	 * @return network driver
	 */
	public String driver();

	/**
	 * Get options map
	 * 
	 * @return Map of option names to option values
	 */
	public Map<String, String> options();

	/**
	 * Get network containers
	 * 
	 * @return Map of container network info
	 */
	public Map<String, IDockerNetworkContainer> containers();

	/**
	 * Get IP Address Management info
	 * 
	 * @return ip address management info
	 */
	public IDockerIpam ipam();

}
