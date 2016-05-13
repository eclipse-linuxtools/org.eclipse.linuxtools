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
