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

package org.eclipse.linuxtools.rpmstubby.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.linuxtools.rpmstubby.StubbyPlugin;


/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {
	
	/**
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = StubbyPlugin.getDefault().getPreferenceStore();
		store.setDefault(PreferenceConstants.P_STUBBY_INTERACTIVE, PreferenceConstants.DP_STUBBY_INTERACTIVE);		
		store.setDefault(PreferenceConstants.P_STUBBY_USE_PDEBUILD_SCRIPT, PreferenceConstants.DP_STUBBY_USE_PDEBUILD_SCRIPT);
	}

}
