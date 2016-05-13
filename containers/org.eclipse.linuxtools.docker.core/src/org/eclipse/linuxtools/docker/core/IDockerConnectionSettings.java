/*******************************************************************************
 * Copyright (c) 2015, 2016 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.docker.core;

/**
 * Settings for an {@link IDockerConnection}
 */
public interface IDockerConnectionSettings {

	enum BindingType {
		UNIX_SOCKET_CONNECTION, TCP_CONNECTION;
	}

	/**
	 * @return the type of binding
	 */
	BindingType getType();

	/**
	 * @return the name of the Docker daemon
	 */
	String getName();

	/**
	 * @return <code>true</code> if the connection could be established and
	 *         Docker daemon responded to a 'ping' request, <code>false</code>
	 *         otherwise.
	 */
	boolean isSettingsResolved();

}
