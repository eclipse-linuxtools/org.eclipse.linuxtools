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

	public void setName(String name) {
		this.name = name;
	}

	@Override
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

}