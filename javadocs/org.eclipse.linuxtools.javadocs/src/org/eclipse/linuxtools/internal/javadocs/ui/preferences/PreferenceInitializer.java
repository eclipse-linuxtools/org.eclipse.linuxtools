/*******************************************************************************
 * Copyright (c) 2011-2015 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
