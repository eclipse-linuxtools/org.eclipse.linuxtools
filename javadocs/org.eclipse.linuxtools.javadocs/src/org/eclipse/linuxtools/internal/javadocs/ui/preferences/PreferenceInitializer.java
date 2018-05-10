/*******************************************************************************
 * Copyright (c) 2011, 2018 Red Hat, Inc.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *     Eric Williams <ericwill@redhat.com> - modification for Javadocs
 *******************************************************************************/
package org.eclipse.linuxtools.internal.javadocs.ui.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.linuxtools.internal.javadocs.ui.JavaDocPlugin;

/**
 * Class used to initialize default preference values.
 * Default: /usr/share/javadoc
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore store = JavaDocPlugin.getDefault().getPreferenceStore();
        store.setDefault(PreferenceConstants.JAVADOCS_DIRECTORY, "/usr/share/javadoc"); //$NON-NLS-1$
    }

}
