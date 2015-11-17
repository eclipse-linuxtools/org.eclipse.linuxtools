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

import static org.eclipse.linuxtools.internal.docker.ui.preferences.PreferenceConstants.AUTOLOG_ON_START;
import static org.eclipse.linuxtools.internal.docker.ui.preferences.PreferenceConstants.DOCKER_MACHINE_INSTALLATION_DIRECTORY;
import static org.eclipse.linuxtools.internal.docker.ui.preferences.PreferenceConstants.LOG_TIMESTAMP;
import static org.eclipse.linuxtools.internal.docker.ui.preferences.PreferenceConstants.REFRESH_TIME;
import static org.eclipse.linuxtools.internal.docker.ui.preferences.PreferenceConstants.VM_DRIVER_INSTALLATION_DIRECTORY;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.linuxtools.internal.docker.ui.utils.SystemUtils;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {


	@Override
	public void initializeDefaultPreferences() {
		final IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(REFRESH_TIME, 15);
		store.setDefault(AUTOLOG_ON_START, true);
		store.setDefault(LOG_TIMESTAMP, true);
		// set docker-machine preferences based on the user's platform
		if (SystemUtils.isWindows()) {
			store.setDefault(DOCKER_MACHINE_INSTALLATION_DIRECTORY,
					"C:\\Program Files\\Docker Toolbox"); //$NON-NLS-1$
			store.setDefault(VM_DRIVER_INSTALLATION_DIRECTORY,
					"C:\\Program Files\\Oracle\\VirtualBox"); //$NON-NLS-1$
		} else if (SystemUtils.isMac()) {
			store.setDefault(DOCKER_MACHINE_INSTALLATION_DIRECTORY,
					"/usr/local/bin"); //$NON-NLS-1$
			store.setDefault(VM_DRIVER_INSTALLATION_DIRECTORY,
					"/usr/local/bin"); //$NON-NLS-1$
		} else if (SystemUtils.isLinux()) {
			store.setDefault(DOCKER_MACHINE_INSTALLATION_DIRECTORY,
					"/usr/local/bin"); //$NON-NLS-1$
			store.setDefault(VM_DRIVER_INSTALLATION_DIRECTORY,
					"/usr/local/bin"); //$NON-NLS-1$
		}
	}

}
