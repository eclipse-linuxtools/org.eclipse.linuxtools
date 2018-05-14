/*******************************************************************************
 * Copyright (c) 2014, 2018 Red Hat.
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

public interface IDockerContainer {

	String id();

	String image();

	String command();

	Long created();

	/**
	 * @return The status string for the container
	 */
	String status();

	List<IDockerPortMapping> ports();

	/**
	 * @return The first name of the container
	 */
	String name();
	
	/**
	 * @return All the names of the container
	 */
	List<String> names();

	Long sizeRw();

	Long sizeRootFs();
	
	/**
	 * @return the {@link IDockerConnection} associated with (or used to retrieve) this {@link IDockerContainer}
	 */
	IDockerConnection getConnection();

	/**
	 * @return the {@link IDockerContainerInfo} by calling the Docker daemon
	 *         using the {@link IDockerConnection} associated with this
	 *         {@link IDockerContainer} if it was not loaded before, otherwise
	 *         uses the previous version.
	 */
	IDockerContainerInfo info();


}
