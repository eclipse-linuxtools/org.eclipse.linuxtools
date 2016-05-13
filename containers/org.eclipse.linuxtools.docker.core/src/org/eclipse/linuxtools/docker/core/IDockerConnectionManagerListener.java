/*******************************************************************************
 * Copyright (c) 2014, 2016 Red Hat.
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

	/**
	 * Notifies the listener that a {@link IDockerConnection} changed.
	 * 
	 * @param type
	 *            the type of change
	 * @deprecated Use
	 *             {@link IDockerConnectionManagerListener2#changeEvent(IDockerConnection, int)}
	 *             instead.
	 */
	@Deprecated
	void changeEvent(int type);

}
