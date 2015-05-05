/*******************************************************************************
 * Copyright (c) 2014 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.ui.preferences;

import static org.eclipse.linuxtools.docker.core.EnumDockerConnectionSettings.BINDING_MODE;
import static org.eclipse.linuxtools.docker.core.EnumDockerConnectionSettings.TCP_CERT_PATH;
import static org.eclipse.linuxtools.docker.core.EnumDockerConnectionSettings.TCP_CONNECTION;
import static org.eclipse.linuxtools.docker.core.EnumDockerConnectionSettings.TCP_HOST;
import static org.eclipse.linuxtools.docker.core.EnumDockerConnectionSettings.TCP_TLS_VERIFY;
import static org.eclipse.linuxtools.docker.core.EnumDockerConnectionSettings.UNIX_SOCKET;
import static org.eclipse.linuxtools.docker.core.EnumDockerConnectionSettings.UNIX_SOCKET_PATH;
import static org.eclipse.linuxtools.internal.docker.ui.preferences.PreferenceConstants.AUTOLOG_ON_START;
import static org.eclipse.linuxtools.internal.docker.ui.preferences.PreferenceConstants.LOG_TIMESTAMP;
import static org.eclipse.linuxtools.internal.docker.ui.preferences.PreferenceConstants.REFRESH_TIME;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#
	 * initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		final IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(REFRESH_TIME, 15);
		// sadly, this will only work if the DOCKER environment variables were set
		// as the system level, not in a .bash_rc or similar terminal session script 
		// which Eclipse cannot access when it is launched from the Dock (or menu).
		final String defaultDockerHost = System.getenv("DOCKER_HOST"); //$NON-NLS-1$
		final String defaultDockerTLSVerify = System.getenv("DOCKER_TLS_VERIFY"); //$NON-NLS-1$
		final String defaultDockerCertPath = System.getenv("DOCKER_CERT_PATH"); //$NON-NLS-1$
		//FIXME: can we use the OS name to provide smarter default values ?
		if (defaultDockerHost == null || defaultDockerHost.equals("")) {
			store.setDefault(BINDING_MODE.toString(), UNIX_SOCKET.toString());
			store.setDefault(UNIX_SOCKET_PATH.toString(), DockerConnection.Defaults.DEFAULT_UNIX_SOCKET_PATH);
		} else if(defaultDockerHost.startsWith("unix://")) {
			store.setDefault(BINDING_MODE.toString(), UNIX_SOCKET.toString());
			store.setDefault(UNIX_SOCKET_PATH.toString(), defaultDockerHost);
		} else {
			store.setDefault(BINDING_MODE.toString(), TCP_CONNECTION.toString());
			store.setDefault(TCP_HOST.toString(), defaultDockerHost);
			if("1".equals(defaultDockerTLSVerify)) {
				store.setDefault(TCP_TLS_VERIFY.toString(), Boolean.TRUE);
			}
			if(defaultDockerCertPath != null && !defaultDockerCertPath.isEmpty()) {
				store.setDefault(TCP_CERT_PATH.toString(), defaultDockerCertPath);
			}
		}
		
		store.setDefault(AUTOLOG_ON_START, true);
		store.setDefault(LOG_TIMESTAMP, true);
	}

}
