package org.eclipse.linuxtools.internal.oprofile.remote.launch.configuration;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.linuxtools.internal.oprofile.launch.configuration.OprofileEventConfigTab;
import org.eclipse.linuxtools.internal.oprofile.launch.configuration.OprofileSetupTab;
import org.eclipse.linuxtools.profiling.launch.RemoteProxyProfileLaunchConfigurationTabGroup;

/**
 * @since 1.1
 */
public class OprofileRemoteLaunchConfigurationTabGroup extends RemoteProxyProfileLaunchConfigurationTabGroup {
	@Override
	public AbstractLaunchConfigurationTab[] getProfileTabs() {
		return new AbstractLaunchConfigurationTab[] { new OprofileSetupTab(), new OprofileEventConfigTab() };
	}
}
