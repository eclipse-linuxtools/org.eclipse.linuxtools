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

import org.eclipse.linuxtools.docker.core.IDockerConnectionSettings;

public abstract class BaseConnectionSettings
		implements IDockerConnectionSettings {

	/** the connection name. */
	private String name = null;

	/** flag indicating if the Docker responded to a ping request. */
	private boolean settingsResolved = false;

	@Override
	@Deprecated
	public String getName() {
		return name;
	}

	public void setSettingsResolved(boolean settingsResolved) {
		this.settingsResolved = settingsResolved;
	}

	@Override
	public boolean isSettingsResolved() {
		return this.settingsResolved;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		BaseConnectionSettings other = (BaseConnectionSettings) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}

}