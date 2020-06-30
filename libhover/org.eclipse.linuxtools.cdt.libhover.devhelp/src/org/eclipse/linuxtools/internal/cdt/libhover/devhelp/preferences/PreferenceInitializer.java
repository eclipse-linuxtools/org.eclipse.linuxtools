/*******************************************************************************
 * Copyright (c) 2011, 2018 Red Hat, Inc.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.cdt.libhover.devhelp.preferences;

import java.io.File;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.linuxtools.internal.cdt.libhover.devhelp.DevHelpPlugin;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {
	
	private final String DEFAULT_DIR = "/usr/share/gtk-doc/html"; //$NON-NLS-1$

    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore store = DevHelpPlugin.getDefault().getPreferenceStore();
        String defaultDir = DEFAULT_DIR;
		File f = new File(defaultDir);
		if (!f.exists() || !f.isDirectory()) {
			defaultDir = ""; //$NON-NLS-1$
		}

        store.setDefault(PreferenceConstants.DEVHELP_DIRECTORY, defaultDir);
    }

}
