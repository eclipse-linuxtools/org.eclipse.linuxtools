/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.logging.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.linuxtools.internal.systemtap.ui.logging.LoggingPlugin;
import org.eclipse.linuxtools.systemtap.ui.logging.LogManager;



public class PreferenceInitializer extends AbstractPreferenceInitializer {
	public void initializeDefaultPreferences() {
		IPreferenceStore store = LoggingPlugin.getDefault().getPreferenceStore();

		//logging
		store.setDefault(PreferenceConstants.P_LOG_ENABLED, false);
		store.setDefault(PreferenceConstants.P_LOG_LEVEL, LogManager.CRITICAL);
		store.setDefault(PreferenceConstants.P_LOG_TYPE, LogManager.CONSOLE);
		store.setDefault(PreferenceConstants.P_LOG_FILE, System.getenv("HOME") + "/systemtapGUI-log");
	}
}
