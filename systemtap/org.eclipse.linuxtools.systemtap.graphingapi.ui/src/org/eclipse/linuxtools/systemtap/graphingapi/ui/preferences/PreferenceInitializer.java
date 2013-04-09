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

package org.eclipse.linuxtools.systemtap.graphingapi.ui.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.linuxtools.internal.systemtap.graphingapi.ui.GraphingAPIUIPlugin;



public class PreferenceInitializer extends AbstractPreferenceInitializer {
	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = GraphingAPIUIPlugin.getDefault().getPreferenceStore();

		store.setDefault(GraphingAPIPreferenceConstants.P_SHOW_X_GRID_LINES, true);
		store.setDefault(GraphingAPIPreferenceConstants.P_SHOW_Y_GRID_LINES, true);
		store.setDefault(GraphingAPIPreferenceConstants.P_AUTO_RESIZE, true);
		store.setDefault(GraphingAPIPreferenceConstants.P_JUMP_NEW_TABLE_ENTRY, false);
		store.setDefault(GraphingAPIPreferenceConstants.P_VIEWABLE_DATA_ITEMS, 100);
		store.setDefault(GraphingAPIPreferenceConstants.P_MAX_DATA_ITEMS, 250);
		store.setDefault(GraphingAPIPreferenceConstants.P_X_SERIES_TICKS, 10);
		store.setDefault(GraphingAPIPreferenceConstants.P_Y_SERIES_TICKS, 4);
	}
}
