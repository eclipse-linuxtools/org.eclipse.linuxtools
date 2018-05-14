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
	 * @return properties array of 2-string arrays (Property/Value) to show in
	 *         Properties view
	 */
	Object[] getProperties();

	/**
	 * @return <code>true</code> if the connection could be established and
	 *         Docker daemon responded to a 'ping' request, <code>false</code>
	 *         otherwise.
	 */
	boolean isSettingsResolved();

}
