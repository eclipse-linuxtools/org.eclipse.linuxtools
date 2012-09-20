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
package org.eclipse.linuxtools.internal.profiling.provider.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.linuxtools.internal.profiling.provider.AbstractProviderPreferencesPage;
import org.eclipse.linuxtools.internal.profiling.provider.ProviderOptionsTab;
import org.eclipse.linuxtools.profiling.launch.ProfileLaunchConfigurationDelegate;
import org.eclipse.linuxtools.profiling.launch.ProfileLaunchConfigurationTabGroup;
import org.eclipse.linuxtools.profiling.launch.ProfileLaunchShortcut;

public abstract class ProviderLaunchConfigurationDelegate extends
		ProfileLaunchConfigurationDelegate {

	@Override
	public void launch(ILaunchConfiguration config, String mode,
			ILaunch launch, IProgressMonitor monitor) {
		try {

			if (config != null) {
				// get provider id from configuration.
				String providerId = config.getAttribute(
						ProviderOptionsTab.PROVIDER_CONFIG_ATT, "");

				// get delegate associated with provider id.
				ProfileLaunchConfigurationDelegate delegate = getConfigurationDelegateFromId(providerId);

				// launch delegate
				if (delegate != null) {
					delegate.launch(config, mode, launch, monitor);
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return;
	}

	/**
	 * Get a provider id to run for the given profiling type.
	 *
	 * This first checks for a provider in the preferences, and if
	 * none can be found it will look for the provider with the
	 * highest priority for the specified type. If this fails,
	 * it will look for the default provider.
	 *
	 * @param type a profiling type
	 * @return a provider id that contributes to the specified type
	 */
	public static String getProviderIdToRun(String type) {
		// Look in the preferences for a provider
		String providerId = ConfigurationScope.INSTANCE.getNode(type).get(
				AbstractProviderPreferencesPage.PREFS_KEY, "");
		if (providerId.equals("") || getConfigurationDelegateFromId(providerId) == null) {
			// Get highest priority provider
			providerId = ProfileLaunchConfigurationTabGroup
					.getHighestProviderId(type);
			if (providerId == null) {
				// Get default provider
				providerId = ProfileLaunchShortcut
						.getDefaultLaunchShortcutProviderId(type);
			}
		}
		return providerId;
	}

	@Override
	public String generateCommand(ILaunchConfiguration config) {
		return null;
	}

	public abstract String getProfilingType();

}
