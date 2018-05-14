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

public interface IDockerContainerListener {

	/**
	 * Called when the list of {@link IDockerContainer} for the given
	 * {@link IDockerConnection} changed (including when it was loaded for the
	 * first time)
	 * 
	 * @param connection
	 *            - the Docker connection
	 * @param containers
	 *            the new list of containers
	 */
	void listChanged(IDockerConnection connection,
			List<IDockerContainer> containers);

}
