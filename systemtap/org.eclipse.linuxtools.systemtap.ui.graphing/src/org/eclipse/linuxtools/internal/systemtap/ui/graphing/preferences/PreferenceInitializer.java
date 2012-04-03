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

package org.eclipse.linuxtools.internal.systemtap.ui.graphing.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.linuxtools.internal.systemtap.ui.graphing.GraphingPlugin;
import org.eclipse.linuxtools.systemtap.ui.logging.LogManager;



public class PreferenceInitializer extends AbstractPreferenceInitializer {
	public void initializeDefaultPreferences() {
		LogManager.logDebug("Start initializeDefaultPreferences:", this);
		IPreferenceStore store = GraphingPlugin.getDefault().getPreferenceStore();

		//graphing
		store.setDefault(GraphingPreferenceConstants.P_GRAPH_UPDATE_DELAY, 1000);
		
/*		These are set in graphingapi.ui.  They shouldn't be reset here

		IPreferenceStore store2 = GraphingAPIUIPlugin.getDefault().getPreferenceStore();
		//graphing.graph
		store2.setDefault(GraphingAPIPreferenceConstants.P_SHOW_GRID_LINES, true);
		store2.setDefault(GraphingAPIPreferenceConstants.P_VIEWABLE_DATA_ITEMS, 100);
		
		//graphing.datatable
		store2.setDefault(GraphingAPIPreferenceConstants.P_AUTO_RESIZE, true);
		store2.setDefault(GraphingAPIPreferenceConstants.P_JUMP_NEW_TABLE_ENTRY, false);
		store2.setDefault(GraphingAPIPreferenceConstants.P_MAX_DATA_ITEMS, 250);
*/		
		LogManager.logDebug("End initializeDefaultPreferences:", this);
	}
}
