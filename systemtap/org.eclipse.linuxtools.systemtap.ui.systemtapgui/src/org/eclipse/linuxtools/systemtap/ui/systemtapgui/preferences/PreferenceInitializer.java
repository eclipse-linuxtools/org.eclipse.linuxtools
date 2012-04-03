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

package org.eclipse.linuxtools.systemtap.ui.systemtapgui.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.linuxtools.internal.systemtap.ui.systemtapgui.SystemTapGUIPlugin;
import org.eclipse.linuxtools.systemtap.ui.logging.LogManager;



public class PreferenceInitializer extends AbstractPreferenceInitializer {
	public void initializeDefaultPreferences() {
		LogManager.logDebug("Start initializeDefaultPreferences:", this);
		IPreferenceStore store = SystemTapGUIPlugin.getDefault().getPreferenceStore();

		//gui
		store.setDefault(PreferenceConstants.P_WINDOW_STATE, true);
	}
}
