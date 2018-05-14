/*******************************************************************************
 * Copyright (c) 2016, 2018 Red Hat.
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

import java.util.Arrays;
import java.util.Collections;
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
			return Arrays.asList(tcp);
		}
		return Collections.emptyList();
	}

}
