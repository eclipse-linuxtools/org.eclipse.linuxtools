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

import java.util.Arrays;
import java.util.Collections;

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
	
	public static IDockerConnectionStorageManager load(IDockerConnection... mockedConnections) {
		final IDockerConnectionStorageManager connectionStorageManager = Mockito
				.mock(IDockerConnectionStorageManager.class);
		Mockito.when(connectionStorageManager.loadConnections()).thenReturn(Arrays.asList(mockedConnections));
		return connectionStorageManager;
	}

}
