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

import java.util.Arrays;
import java.util.List;

import org.eclipse.linuxtools.docker.core.IDockerConnectionSettings;
import org.eclipse.linuxtools.docker.core.IDockerConnectionSettingsProvider;

public class SystemConnectionSettingsProvider implements IDockerConnectionSettingsProvider {

	@Override
	public List<IDockerConnectionSettings> getConnectionSettings() {
		final String dockerHostEnv = System.getenv(DefaultDockerConnectionSettingsFinder.DOCKER_HOST);
		if (dockerHostEnv != null) {
			String pathToCertificates = System.getenv(DefaultDockerConnectionSettingsFinder.DOCKER_CERT_PATH);
			TCPConnectionSettings tcp = new TCPConnectionSettings(dockerHostEnv, pathToCertificates);
			return Arrays.asList(new IDockerConnectionSettings[] { tcp });
		}
		return null;
	}

}
