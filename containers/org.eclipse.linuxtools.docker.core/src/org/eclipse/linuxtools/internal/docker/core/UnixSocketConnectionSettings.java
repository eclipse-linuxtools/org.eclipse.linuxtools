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

	@Override
	public Object[] getProperties() {
		return new Object[] {
				new Object[] { "Type", this.getType().toString() }, //$NON-NLS-1$
				new Object[] { "Socket", this.getPath() }, //$NON-NLS-1$
		};
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
