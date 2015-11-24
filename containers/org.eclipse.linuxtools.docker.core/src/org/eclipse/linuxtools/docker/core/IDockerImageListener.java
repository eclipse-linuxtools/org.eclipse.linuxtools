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
