/*******************************************************************************
 * Copyright (c) 2014 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.ui.preferences;

import static org.eclipse.linuxtools.internal.docker.ui.preferences.PreferenceConstants.AUTOLOG_ON_START;
import static org.eclipse.linuxtools.internal.docker.ui.preferences.PreferenceConstants.LOG_TIMESTAMP;
import static org.eclipse.linuxtools.internal.docker.ui.preferences.PreferenceConstants.REFRESH_TIME;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.linuxtools.docker.ui.Activator;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		final IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(REFRESH_TIME, 15);
		store.setDefault(AUTOLOG_ON_START, true);
		store.setDefault(LOG_TIMESTAMP, true);
	}

}
