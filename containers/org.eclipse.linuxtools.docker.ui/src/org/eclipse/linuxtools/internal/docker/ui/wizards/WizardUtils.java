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

package org.eclipse.linuxtools.internal.docker.ui.wizards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerContainer;

/**
 * @author xcoulon
 *
 */
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
