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

public interface IDockerImageListener {

	/**
	 * Called when the list of {@link IDockerImage} for the given
	 * {@link IDockerConnection} changed
	 * 
	 * @param connection
	 *            the {@link IDockerConnection} in which the list of
	 *            {@link IDockerImage} changed
	 * @param images
	 *            the new list of {@link IDockerImage}
	 */
	void listChanged(IDockerConnection connection, List<IDockerImage> images);

}
