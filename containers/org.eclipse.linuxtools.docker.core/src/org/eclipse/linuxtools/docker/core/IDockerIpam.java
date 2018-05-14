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

import java.util.List;

/**
 * IP Address Management
 * 
 * Corresponds to docker client IPAM which is needed to create a Docker Network
 * config
 * 
 * @author jjohnstn
 *
 */
public interface IDockerIpam {

	/**
	 * Get driver
	 * 
	 * @return String containing driver
	 */
	String driver();

	/**
	 * Get IPAM configurations
	 * 
	 * @return List of IPAM configurations
	 */
	List<IDockerIpamConfig> config();

}
