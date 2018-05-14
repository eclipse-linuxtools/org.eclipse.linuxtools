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
 * Docker Network Creation status
 * 
 * @author jjohnstn
 *
 */
public interface IDockerNetworkCreation {

	/**
	 * Get network id
	 * 
	 * @return the id string for the network
	 */
	String id();

	/**
	 * Get network creation warnings
	 * 
	 * @return String containing warnings
	 */
	String warnings();

}
