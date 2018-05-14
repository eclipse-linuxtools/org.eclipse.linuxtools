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

public interface IDockerVersion {

	/**
	 * Get the connection
	 * 
	 * @return the {@link IDockerConnection} for this version info
	 */
	IDockerConnection getConnection();

	/**
	 * Get the api version
	 * 
	 * @return the api version of the Docker daemon
	 */
	String apiVersion();

	/**
	 * Get the Docker daemon architecture
	 * 
	 * @return the architecture of the Docker daemon
	 */
	String arch();

	/**
	 * Get the git commit for the Docker daemon
	 * 
	 * @return the last git commit which is part of the Docker daemon code
	 */
	String gitCommit();

	/**
	 * Get the go version
	 * 
	 * @return the version of go used by the Docker daemon
	 */
	String goVersion();

	/**
	 * Get the kernel version
	 * 
	 * @return the kernel version that the Docker daemon is running on
	 */
	String kernelVersion();

	/**
	 * Get the OS
	 * 
	 * @return the OS that Docker daemon is running on
	 */
	String os();

	/**
	 * Get the Docker daemon version
	 * 
	 * @return the Docker daemon version
	 */
	String version();
}
