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
 * IP Address Management Configuration
 * 
 * Used to create a docker client IPAM
 * 
 */
public interface IDockerIpamConfig {
	/**
	 * Get subnet
	 * 
	 * @return String containing subnet
	 */
	String subnet();

	/**
	 * Set subnet
	 * 
	 * @param subnet
	 *            subnet string
	 */
	void subnet(final String subnet);

	/**
	 * Get ip range
	 * 
	 * @return String containing ip range
	 */
	String ipRange();

	/**
	 * Set ip range
	 * 
	 * @param ipRange
	 *            ip range string
	 */
	void ipRange(final String ipRange);

	/**
	 * Get gateway
	 * 
	 * @return String containing gateway
	 */
	String gateway();

	/**
	 * Set gateway
	 * 
	 * @param gateway
	 *            string containing gateway
	 */
	void gateway(final String gateway);
}
