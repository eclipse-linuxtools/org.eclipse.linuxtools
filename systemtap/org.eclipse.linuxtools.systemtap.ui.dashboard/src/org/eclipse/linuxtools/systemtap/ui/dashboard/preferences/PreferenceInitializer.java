/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse, Anithra P J
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.ui.dashboard.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.linuxtools.systemtap.ui.dashboard.internal.DashboardPlugin;

public class PreferenceInitializer extends AbstractPreferenceInitializer {
	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = DashboardPlugin.getDefault().getPreferenceStore();

		//dashboard
		store.setDefault(DashboardPreferenceConstants.P_MODULE_FOLDERS, ""); //$NON-NLS-1$
		store.setDefault(DashboardPreferenceConstants.P_DASHBOARD_UPDATE_DELAY, 1000);
		store.setDefault(DashboardPreferenceConstants.P_DASHBOARD_EXAMPLES_DIR, "/usr/local/share/doc/systemtap/examples"); //$NON-NLS-1$
	}
}
