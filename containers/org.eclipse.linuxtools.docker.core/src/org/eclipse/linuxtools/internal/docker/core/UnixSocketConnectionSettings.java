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
	public UnixSocketConnectionSettings(final String path) {
		super();
		if (path != null && !path.isEmpty() && !path.matches("\\w+://.*")) { //$NON-NLS-1$
			this.path = "unix://" + path; //$NON-NLS-1$
		} else {
			this.path = path;
		}
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

	public boolean hasPath() {
		return this.path != null && !this.path.isEmpty();
	}

	@Override
	public String toString() {
		return this.path;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		UnixSocketConnectionSettings other = (UnixSocketConnectionSettings) obj;
		if (path == null) {
			if (other.path != null) {
				return false;
			}
		} else if (!path.equals(other.path)) {
			return false;
		}
		return true;
	}


}
