/*******************************************************************************
 * Copyright (c) 2015, 2018 Red Hat.
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

/**
 * Manager that takes care of loading and saving connections settings in a file.
 */
public interface IDockerConnectionStorageManager {

	/**
	 * Loads connections from the underlying file
	 * 
	 * @return {@link List} of {@link IDockerConnection}
	 */
	List<IDockerConnection> loadConnections();

	/**
	 * Saves the given {@link List} of {@link IDockerConnection} into a file
	 * 
	 * @param connections
	 *            the connections to save
	 */
	void saveConnections(List<IDockerConnection> connections);

}
