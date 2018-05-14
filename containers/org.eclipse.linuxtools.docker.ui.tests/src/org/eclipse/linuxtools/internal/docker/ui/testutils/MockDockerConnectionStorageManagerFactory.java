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

package org.eclipse.linuxtools.internal.docker.ui.testutils;

import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerConnectionStorageManager;
import org.mockito.Mockito;

/**
 * Factory for mocked {@link IDockerConnection}
 */
public class MockDockerConnectionStorageManagerFactory {

	public static IDockerConnectionStorageManager loadNone() {
		final IDockerConnectionStorageManager connectionStorageManager = Mockito
				.mock(IDockerConnectionStorageManager.class);
		Mockito.when(connectionStorageManager.loadConnections()).thenReturn(Collections.emptyList());
		return connectionStorageManager;
	}

	public static IDockerConnectionStorageManager providing(final IDockerConnection... mockedConnections) {
		final IDockerConnectionStorageManager connectionStorageManager = Mockito
				.mock(IDockerConnectionStorageManager.class);
		Mockito.when(connectionStorageManager.loadConnections()).thenReturn(Stream.of(mockedConnections).collect(Collectors.toList()));
		return connectionStorageManager;
	}

}
