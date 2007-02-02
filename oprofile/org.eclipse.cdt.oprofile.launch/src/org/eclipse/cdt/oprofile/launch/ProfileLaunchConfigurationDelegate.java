/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/

package org.eclipse.cdt.oprofile.launch;

import org.eclipse.cdt.launch.AbstractCLaunchDelegate;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * This is the delegation class for the profile launcher. It handles the request to
 * run the launch configuration.
 * @author keiths
 */
public class ProfileLaunchConfigurationDelegate extends AbstractCLaunchDelegate
{
	/**
	 * @see org.eclipse.debug.core.model.ILaunchConfigurationDelegate#launch(ILaunchConfiguration, String, ILaunch, IProgressMonitor)
	 */
	public void launch(ILaunchConfiguration config, String mode, ILaunch launch,
									IProgressMonitor monitor) throws CoreException
	{
		OprofileSession session = new OprofileSession(config);
		session.run();
	}

	/**
	 * @see org.eclipse.cdt.launch.AbstractCLaunchDelegate#getPluginID()
	 */
	protected String getPluginID()
	{
		return LaunchPlugin.getUniqueIdentifier();
	}

}
