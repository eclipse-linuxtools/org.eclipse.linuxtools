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

package org.eclipse.linuxtools.internal.docker.ui.testutils;

import java.util.Collections;

import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.mockito.Mockito;

/**
 * Factory for mocked {@link IDockerConnection}
 */
public class MockDockerConnectionFactory {

	public static IDockerConnection noImageNoContainer(final String name) {
		final IDockerConnection connection = Mockito
				.mock(IDockerConnection.class);
		Mockito.when(connection.getName()).thenReturn(name);
		noImageAvailable(connection);
		noContainerAvailable(connection);
		return connection;
	}
	
	private static void noImageAvailable(final IDockerConnection connection) {
		Mockito.when(connection.getImages()).thenReturn(Collections.emptyList());
	}

	private static void noContainerAvailable(final IDockerConnection connection) {
		Mockito.when(connection.getContainers()).thenReturn(Collections.emptyList());
	}
}
