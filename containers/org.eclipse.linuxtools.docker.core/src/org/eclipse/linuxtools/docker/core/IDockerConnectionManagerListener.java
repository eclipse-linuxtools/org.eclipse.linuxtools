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

public interface IDockerConnectionManagerListener {

	int ADD_EVENT = 0;
	int REMOVE_EVENT = 1;
	int RENAME_EVENT = 2;
	int UPDATE_SETTINGS_EVENT = 3;
	/**
	 * @since 4.0
	 */
	int ENABLE_EVENT = 4;
	/**
	 * @since 4.0
	 */
	int DISABLE_EVENT = 5;

	/**
	 * Notifies the listener that the given {@link IDockerConnection} changed.
	 * 
	 * @param connection
	 *            the connection that changed
	 * @param type
	 *            the type of change
	 * 
	 * @since 3.0
	 */
	void changeEvent(IDockerConnection connection, int type);

}
