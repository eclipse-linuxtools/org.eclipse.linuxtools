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
 * IP Address Management Configuration
 * 
 * Used to create a docker client IPAM
 * 
 * @author jjohnstn
 *
 */
public interface IDockerIpamConfig {
	/**
	 * Get subnet
	 * 
	 * @return String containing subnet
	 */
	public String subnet();

	/**
	 * Set subnet
	 * 
	 * @param subnet
	 *            subnet string
	 */
	public void subnet(final String subnet);

	/**
	 * Get ip range
	 * 
	 * @return String containing ip range
	 */
	public String ipRange();

	/**
	 * Set ip range
	 * 
	 * @param ipRange
	 *            ip range string
	 */
	public void ipRange(final String ipRange);

	/**
	 * Get gateway
	 * 
	 * @return String containing gateway
	 */
	public String gateway();

	/**
	 * Set gateway
	 * 
	 * @param gateway
	 *            string containing gateway
	 */
	public void gateway(final String gateway);
}
