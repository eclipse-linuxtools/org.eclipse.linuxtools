/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kent Sebastian <ksebasti@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.oprofile.launch.launching;

import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.linuxtools.internal.oprofile.launch.OprofileLaunchPlugin;
import org.eclipse.linuxtools.internal.oprofile.launch.configuration.LaunchOptions;
import org.eclipse.linuxtools.profiling.launch.ProfileLaunchShortcut;

/**
 * A class that takes care of the right-click -> profile with oprofile
 *   shortcut, where the ProfileLaunchShortcut has the logic to automatically
 *   find binaries and create a default launch if one doesn't exist.
 */
public class OprofileManualLaunchShortcut extends ProfileLaunchShortcut {
	@Override
	protected ILaunchConfigurationType getLaunchConfigType() {
		return getLaunchManager().getLaunchConfigurationType(OprofileLaunchPlugin.ID_LAUNCH_PROFILE_MANUAL);
	}

	/**
	 * Default settings for the OProfile-specific option tabs.
	 */
	@Override
	protected void setDefaultProfileAttributes(ILaunchConfigurationWorkingCopy wc) {
		//default global setup options
		LaunchOptions options = new LaunchOptions();
		options.saveConfiguration(wc);

		//default event option
		wc.setAttribute(OprofileLaunchPlugin.ATTR_USE_DEFAULT_EVENT, true);
	}

	@Override
	protected ILaunchConfiguration findLaunchConfiguration(IBinary bin, String mode) {
		ILaunchConfiguration config = super.findLaunchConfiguration(bin, mode);

		//hijack the launch config and add in the manual profile value, which will be
		// used in the delegate
		try {
			ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
			wc.setAttribute(OprofileLaunchPlugin.ATTR_MANUAL_PROFILE, true);
			wc.doSave();
		} catch (CoreException e) {
			e.printStackTrace();
		}

		return config;
	}
}
