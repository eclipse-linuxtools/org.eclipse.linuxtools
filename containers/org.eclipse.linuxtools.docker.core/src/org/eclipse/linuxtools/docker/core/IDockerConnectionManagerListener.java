/*******************************************************************************
 * Copyright (c) 2014, 2017 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
