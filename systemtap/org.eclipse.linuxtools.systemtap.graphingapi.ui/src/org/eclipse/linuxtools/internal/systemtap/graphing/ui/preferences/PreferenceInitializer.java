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

package org.eclipse.linuxtools.internal.systemtap.graphing.ui.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.linuxtools.internal.systemtap.graphing.ui.GraphingUIPlugin;



public class PreferenceInitializer extends AbstractPreferenceInitializer {
	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = GraphingUIPlugin.getDefault().getPreferenceStore();

		//graphing
		store.setDefault(GraphingPreferenceConstants.P_GRAPH_UPDATE_DELAY, 1000);

		//data table
		store.setDefault(GraphingPreferenceConstants.P_AUTO_RESIZE, true);
		store.setDefault(GraphingPreferenceConstants.P_JUMP_NEW_TABLE_ENTRY, false);
		store.setDefault(GraphingPreferenceConstants.P_MAX_DATA_ITEMS, 250);

		//graph
		store.setDefault(GraphingPreferenceConstants.P_SHOW_X_GRID_LINES, true);
		store.setDefault(GraphingPreferenceConstants.P_SHOW_Y_GRID_LINES, true);
		store.setDefault(GraphingPreferenceConstants.P_VIEWABLE_DATA_ITEMS, 100);
		store.setDefault(GraphingPreferenceConstants.P_X_SERIES_TICKS, 64);
		store.setDefault(GraphingPreferenceConstants.P_Y_SERIES_TICKS, 64);
	}
}
