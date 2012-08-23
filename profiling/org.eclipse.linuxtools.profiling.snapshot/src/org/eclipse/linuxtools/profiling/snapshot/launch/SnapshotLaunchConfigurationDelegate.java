/*******************************************************************************
 * Copyright (c) 2012 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.profiling.snapshot.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.linuxtools.profiling.launch.ProfileLaunchConfigurationDelegate;
import org.eclipse.linuxtools.profiling.launch.ProfileLaunchConfigurationTabGroup;
import org.eclipse.linuxtools.profiling.launch.ProfileLaunchShortcut;
import org.eclipse.linuxtools.profiling.snapshot.SnapshotPreferencesPage;

public class SnapshotLaunchConfigurationDelegate extends
		ProfileLaunchConfigurationDelegate {

	private static final String SNAPSHOT = "snapshot"; //$NON-NLS-1$

	@Override
	protected String getPluginID() {
		return "org.eclipse.linuxtools.profiling.snapshot";
	}

	@Override
	public void launch(ILaunchConfiguration config, String mode,
			ILaunch launch, IProgressMonitor monitor) {
		try {

			if (config != null) {
				// get provider id from configuration.
				String providerId = config.getAttribute("provider", "");
				if (providerId.equals("")) {
					providerId = getProviderIdToRun();
				}
				// get configuration delegate associated with provider id.
				ProfileLaunchConfigurationDelegate delegate = getConfigurationDelegateFromId(providerId);
				if (delegate != null) {
					delegate.launch(config, mode, launch, monitor);
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return;
	}

	private String getProviderIdToRun() {
		// Get self assigned default
		String providerId = ConfigurationScope.INSTANCE.getNode(SnapshotPreferencesPage.QUALIFIER).get(SnapshotPreferencesPage.KEY, "");
		if (providerId == "") {
			providerId = ProfileLaunchConfigurationTabGroup
					.getHighestProviderId(SNAPSHOT);
			if (providerId == "") {
			// Get highest priority provider
			providerId = ProfileLaunchShortcut
					.getDefaultLaunchShortcutProviderId(SNAPSHOT);
			}
		}
		return providerId;
	}

	@Override
	public String generateCommand(ILaunchConfiguration config) {
		return null;
	}

}
