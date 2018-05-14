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
