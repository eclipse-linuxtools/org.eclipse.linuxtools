/*******************************************************************************
 * Copyright (c) 2006, 2018 IBM Corporation and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.ui.consolelog.preferences;



import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.linuxtools.systemtap.ui.consolelog.internal.ConsoleLogPlugin;



public class PreferenceInitializer extends AbstractPreferenceInitializer {
    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore store = ConsoleLogPlugin.getDefault().getPreferenceStore();

        //ide
        store.setDefault(ConsoleLogPreferenceConstants.HOST_NAME, "localhost"); //$NON-NLS-1$
        store.setDefault(ConsoleLogPreferenceConstants.PORT_NUMBER, 22462);
        store.setDefault(ConsoleLogPreferenceConstants.SCP_USER, "guest"); //$NON-NLS-1$
        store.setDefault(ConsoleLogPreferenceConstants.SCP_PASSWORD, "welcome"); //$NON-NLS-1$


    }
}
