/*******************************************************************************
 * Copyright (c) 2009, 2018 Red Hat, Inc.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alexander Kurtakov - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.core;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.linuxtools.rpm.core.IRPMConstants;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

/**
 * Initialize preferences.
 *
 */
public class RPMCoreInitializer extends AbstractPreferenceInitializer {

    @Override
    public void initializeDefaultPreferences() {
		IPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, IRPMConstants.RPM_CORE_ID);
		String userName = System.getProperty("user.name"); //$NON-NLS-1$
		store.setDefault(IRPMConstants.RPM_DISPLAYED_LOG_NAME, ".logfilename_" //$NON-NLS-1$
				+ userName);
		store.setDefault(IRPMConstants.RPM_LOG_NAME, "rpmbuild.log"); //$NON-NLS-1$

		store.setDefault(IRPMConstants.RPM_CMD, "rpm"); //$NON-NLS-1$
		store.setDefault(IRPMConstants.RPMBUILD_CMD, "rpmbuild"); //$NON-NLS-1$
		store.setDefault(IRPMConstants.DIFF_CMD, "diff"); //$NON-NLS-1$
    }

}
