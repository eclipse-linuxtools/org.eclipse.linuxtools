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

package org.eclipse.linuxtools.internal.docker.core;

/**
 * Unix Socket Connection Settings
 */
public class UnixSocketConnectionSettings extends BaseConnectionSettings {

	/** The path to the Unix Socket, including scheme (unix://). */
	private final String path;

	/**
	 * Constructor
	 * 
	 * @param path
	 *            the path to the Unix Socket
	 */
	public UnixSocketConnectionSettings(String path) {
		super();
		this.path = path;
	}

	@Override
	public BindingType getType() {
		return BindingType.UNIX_SOCKET_CONNECTION;
	}

	/**
	 * @return the path to the Unix Socket
	 */
	public String getPath() {
		return path;
	}

}
