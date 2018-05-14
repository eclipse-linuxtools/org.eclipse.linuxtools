/*******************************************************************************
 * Copyright (c) 2015,2018 Red Hat.
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

package org.eclipse.linuxtools.internal.docker.ui.wizards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerContainer;

public class WizardUtils {

	/**
	 * @param connection
	 *            the current {@link IDockerContainer}
	 * @return the {@link List} of name of the {@link IDockerContainer}
	 */
	public static List<String> getContainerNames(
			final IDockerConnection connection) {
		final List<String> containerNames = new ArrayList<>();
		for (IDockerContainer container : connection.getContainers()) {
			containerNames.add(container.name());
		}
		Collections.sort(containerNames);
		return containerNames;
	}

	/**
	 * Finds and returns the {@link IDockerContainer} with the given name
	 * 
	 * @param connection
	 *            the current {@link IDockerConnection}
	 * @param containerName
	 *            the {@link IDockerContainer} name
	 * @return the matching {@link IDockerContainer} of <code>null</code> if
	 *         none was found.
	 */
	public static IDockerContainer getContainer(
			final IDockerConnection connection, final String containerName) {
		if (containerName != null && !containerName.isEmpty()) {
			for (IDockerContainer container : connection.getContainers()) {
				if (container.name().equals(containerName)) {
					return container;
				}
			}
		}
		return null;

	}

}
