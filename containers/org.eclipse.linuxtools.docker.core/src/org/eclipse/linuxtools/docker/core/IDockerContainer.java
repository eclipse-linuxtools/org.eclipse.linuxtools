/*******************************************************************************
 * Copyright (c) 2014 Red Hat.
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

	public String id();

	public String image();

	public String command();

	public Long created();

	/**
	 * @return The status string for the container
	 */
	public String status();

	public List<IDockerPortMapping> ports();

	/**
	 * @return The first name of the container
	 */
	public String name();
	
	/**
	 * @return All the names of the container
	 */
	public List<String> names();

	public Long sizeRw();

	public Long sizeRootFs();
	
	/**
	 * @return the {@link IDockerConnection} associated with (or used to retrieve) this {@link IDockerContainer}
	 */
	public IDockerConnection getConnection();

	/**
	 * @return <code>true</code> if the {@link IDockerContainerInfo} has been
	 *         loaded, <code>false</code> otherwise.
	 */
	public boolean isInfoLoaded();

	/**
	 * @return the {@link IDockerContainerInfo} by calling the Docker daemon
	 *         using the {@link IDockerConnection} associated with this
	 *         {@link IDockerContainer} if it was not loaded before, otherwise
	 *         uses the previous version.
	 */
	public IDockerContainerInfo info();

	/**
	 * @param force
	 *            <code>true</code> to force refresh, <code>false</code> to use
	 *            existing {@link IDockerContainerInfo} if it was loaded before.
	 * @return the {@link IDockerContainerInfo} by calling the Docker daemon
	 *         using the {@link IDockerConnection} associated with this
	 *         {@link IDockerContainer}.
	 */
	public IDockerContainerInfo info(boolean force);
	

}
