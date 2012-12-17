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
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.linuxtools.internal.profiling.launch.provider.ProviderProfileConstants;
import org.eclipse.linuxtools.profiling.launch.ProfileLaunchConfigurationDelegate;

public class ProviderLaunchConfigurationDelegate extends
		ProfileLaunchConfigurationDelegate {

	@Override
	public void launch(ILaunchConfiguration config, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {

		if (config != null) {
			// get provider id from configuration.
			String providerId = config.getAttribute(
					ProviderProfileConstants.PROVIDER_CONFIG_ATT, "");

			// get delegate associated with provider id.
			ProfileLaunchConfigurationDelegate delegate = ProviderFramework
					.getConfigurationDelegateFromId(providerId);

			// launch delegate
			if (delegate != null) {
				delegate.launch(config, mode, launch, monitor);
			}
		}
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
