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
