/*******************************************************************************
 * Copyright (c) 2016 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.ui.propertytesters;

import java.util.List;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.docker.core.EnumDockerConnectionState;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.internal.docker.ui.DockerConnectionWatcher;
import org.eclipse.ui.PlatformUI;

public class ConnectionPropertyTester extends PropertyTester {

	/** Property name to check if an active {@link IDockerConnection} exists. */
	public static final String EXISTS_ACTIVE_CONNECTION = "existsActiveConnection"; //$NON-NLS-1$

	/** Property name to check if the selected (parent) {@link IDockerConnection} is open. */
	public static final String CONNECTION_ESTABLISHED = "connectionIsEstablished"; //$NON-NLS-1$

	/** Property name to check if any {@link IDockerConnection} exists. */
	@Deprecated
	public static final String EXISTS_ANY_CONNECTION = "existsAnyConnection"; //$NON-NLS-1$

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		// If no active workbench window, we can't check for active connection
		if (PlatformUI.getWorkbench().getActiveWorkbenchWindow() == null) {
			return expectedValue.equals(false);
		}
		switch (property) {
		case EXISTS_ACTIVE_CONNECTION:
			return expectedValue.equals(DockerConnectionWatcher.getInstance().getConnection() != null);
		case EXISTS_ANY_CONNECTION:
			return expectedValue.equals(
					DockerConnectionManager.getInstance().hasConnections());
		case CONNECTION_ESTABLISHED:
			return isConnectionEstablished(receiver, property, args,
					expectedValue);
		}
		return false;
	}

	private boolean isConnectionEstablished(Object receiver, String property,
			Object[] args, Object expectedValue) {
		if (receiver instanceof List && !((List<?>) receiver).isEmpty()) {
			return isConnectionEstablished(((List<?>) receiver).get(0),
					property, args,
					expectedValue);
		}
		if (receiver instanceof IAdaptable) {
			final IDockerConnection connection = ((IAdaptable) receiver)
					.getAdapter(IDockerConnection.class);
			if (connection != null) {
				return isConnectionEstablished(connection, property, args,
						expectedValue);
			}
		}
		if (receiver instanceof IDockerConnection) {
			return expectedValue.equals(((IDockerConnection) receiver)
					.getState() == EnumDockerConnectionState.ESTABLISHED);
		}
		return false;
	}

}
