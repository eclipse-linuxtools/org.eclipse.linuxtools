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
