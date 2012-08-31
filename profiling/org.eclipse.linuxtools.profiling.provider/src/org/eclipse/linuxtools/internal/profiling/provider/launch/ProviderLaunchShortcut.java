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

import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.linuxtools.internal.profiling.provider.AbstractProviderPreferencesPage;
import org.eclipse.linuxtools.profiling.launch.ProfileLaunchConfigurationTabGroup;
import org.eclipse.linuxtools.profiling.launch.ProfileLaunchShortcut;

public abstract class ProviderLaunchShortcut extends ProfileLaunchShortcut {

	@Override
	public void launch(IBinary bin, String mode) {
		String providerId = getProviderIdToRun();
		ProfileLaunchShortcut shortcut = ProfileLaunchShortcut
				.getLaunchShortcutProviderFromId(providerId);
		if (shortcut != null) {
			shortcut.launch(bin, mode);
		} else {
			handleFail(Messages.ProviderLaunchShortcut_0 + getProfilingType());
		}
	}

	@Override
	protected void setDefaultProfileAttributes(
			ILaunchConfigurationWorkingCopy wc) {
		wc.setAttribute(IDebugUIConstants.ATTR_LAUNCH_IN_BACKGROUND, false);
		wc.setAttribute(IDebugUIConstants.ATTR_CAPTURE_IN_CONSOLE, true);
	}

	/**
	 * Get id of provider to launch.
	 *
	 * @return unique id of provider to launch.
	 */
	private String getProviderIdToRun() {
		String profilingType = getProfilingType();
		// Get launch provider id from preferences.
		String providerId = ConfigurationScope.INSTANCE.getNode(
				profilingType).get(
				AbstractProviderPreferencesPage.PREFS_KEY, "");
		if (providerId.equals("")) {
			// Get highest priority launch provider id.
			providerId = ProfileLaunchConfigurationTabGroup
					.getHighestProviderId(profilingType);
			if (providerId.equals("")) {
				// Get self assigned default.
				providerId = ProfileLaunchShortcut
						.getDefaultLaunchShortcutProviderId(profilingType);
			}
		}
		return providerId;
	}

	/**
	 * Get profiling type of this plug-in.
	 *
	 * @return String profiling type this plug-in supports.
	 */
	protected abstract String getProfilingType();

	@Override
	protected ILaunchConfigurationType getLaunchConfigType() {
		// Not needed since we are overriding launch method.
		return null;
	}
}
