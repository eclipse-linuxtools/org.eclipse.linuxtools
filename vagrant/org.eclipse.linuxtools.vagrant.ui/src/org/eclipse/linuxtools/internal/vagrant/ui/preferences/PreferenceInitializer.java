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
package org.eclipse.linuxtools.internal.vagrant.ui.preferences;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.linuxtools.internal.vagrant.ui.Activator;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	public static final String VAGRANT_PATH = "vagrantPath"; //$NON-NLS-1$

	@Override
	public void initializeDefaultPreferences() {
		final String os = Platform.getOS();
		final IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		if (os.equals(Platform.OS_WIN32)) {
			store.setDefault(VAGRANT_PATH, "C:\\Program Files\\Vagrant"); //$NON-NLS-1$
		} else if (os.equals(Platform.OS_MACOSX)) {
			store.setDefault(VAGRANT_PATH, "/usr/local/bin"); //$NON-NLS-1$
		} else if (os.equals(Platform.OS_LINUX)) {
			store.setDefault(VAGRANT_PATH, "/usr/local/bin"); //$NON-NLS-1$
		}
	}

}

