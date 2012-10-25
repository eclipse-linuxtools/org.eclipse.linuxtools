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
package org.eclipse.linuxtools.internal.profiling.launch.provider.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.linuxtools.internal.profiling.launch.provider.ProviderProfileConstants;
import org.eclipse.linuxtools.profiling.launch.ProfileLaunchConfigurationDelegate;

public class ProviderLaunchConfigurationDelegate extends
		ProfileLaunchConfigurationDelegate {

	@Override
	public void launch(ILaunchConfiguration config, String mode,
			ILaunch launch, IProgressMonitor monitor) {
		try {

			if (config != null) {
				// get provider id from configuration.
				String providerId = config.getAttribute(
						ProviderProfileConstants.PROVIDER_CONFIG_ATT, "");

				// get delegate associated with provider id.
				ProfileLaunchConfigurationDelegate delegate = ProviderFramework.getConfigurationDelegateFromId(providerId);

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
		String providerId = ConfigurationScope.INSTANCE.getNode(
				ProviderProfileConstants.PLUGIN_ID).get(
				ProviderProfileConstants.PREFS_KEY + type, "");
		if (providerId.equals("") || ProviderFramework.getConfigurationDelegateFromId(providerId) == null) {
			// Get highest priority provider
			providerId = ProviderFramework
					.getHighestProviderId(type);
		}
		return providerId;
	}

	@Override
	public String generateCommand(ILaunchConfiguration config) {
		return null;
	}

	@Override
	protected String getPluginID() {
		return ProviderProfileConstants.PLUGIN_ID;
	}

}
