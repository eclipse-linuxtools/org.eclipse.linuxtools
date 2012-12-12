/*******************************************************************************
 * Copyright (c) 2007 Alphonse Van Assche.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alphonse Van Assche - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.rpm.rpmlint.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.linuxtools.internal.rpm.rpmlint.Activator;
import org.eclipse.ui.preferences.ScopedPreferenceStore;


/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {
	
	/**
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE,Activator.PLUGIN_ID);
		store.setDefault(PreferenceConstants.P_RPMLINT_PATH, PreferenceConstants.DP_RPMLINT_PATH);
		store.setDefault(PreferenceConstants.P_RPMLINT_TABS_AND_SPACES, PreferenceConstants.P_RPMLINT_SPACES);		
		
	}

}
