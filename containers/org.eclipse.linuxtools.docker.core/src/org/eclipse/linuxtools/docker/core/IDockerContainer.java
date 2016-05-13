/*******************************************************************************
 * Copyright (c) 2014, 2016 Red Hat.
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
