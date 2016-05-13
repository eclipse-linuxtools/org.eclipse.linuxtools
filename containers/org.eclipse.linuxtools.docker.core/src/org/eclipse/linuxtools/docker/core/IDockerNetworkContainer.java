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

/**
 * Container Network info
 * 
 * @author jjohnstn
 *
 */
public interface IDockerNetworkContainer {

	/**
	 * Get endpoint id
	 * 
	 * @return endpoint id
	 */
	String endpointId();

	/**
	 * Get mac address
	 * 
	 * @return mac address
	 */
	String macAddress();

	/**
	 * Get ipv4 address
	 * 
	 * @return ipv4 address
	 */
	String ipv4address();

	/**
	 * Get ipv6 address
	 * 
	 * @return ipv6 address
	 */
	String ipv6address();

}
