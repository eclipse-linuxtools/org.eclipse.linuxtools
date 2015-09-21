/*******************************************************************************
 * Copyright (c) 2015 Red Hat.
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

/**
 * Manager that takes care of loading and saving connections settings in a file.
 */
public interface IDockerConnectionStorageManager {

	/**
	 * Loads connections from the underlying file
	 * 
	 * @return {@link List} of {@link IDockerConnection}
	 */
	public List<IDockerConnection> loadConnections();

	/**
	 * Saves the given {@link List} of {@link IDockerConnection} into a file
	 * 
	 * @param connections
	 *            the connections to save
	 */
	public void saveConnections(List<IDockerConnection> connections);

}
