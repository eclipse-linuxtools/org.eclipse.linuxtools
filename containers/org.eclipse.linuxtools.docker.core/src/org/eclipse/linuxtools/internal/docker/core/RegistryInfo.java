/*******************************************************************************
 * Copyright (c) 2016 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.core;

import org.eclipse.linuxtools.docker.core.IRegistry;

public class RegistryInfo implements IRegistry {

	private String serverAddress;

	public RegistryInfo(String serverAddress) {
		this.serverAddress = serverAddress;
	}

	@Override
	public String getServerAddress() {
		return serverAddress;
	}

}
