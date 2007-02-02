/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/

package org.eclipse.cdt.oprofile.launch;

import org.eclipse.cdt.oprofile.core.Oprofile;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;

/**
 * This class defines the launch tab group that is displayed by the
 * launch manager.
 * @author Keith Seitz <keiths@redhat.com>
 */
public class LaunchConfigurationTabGroup
	extends AbstractLaunchConfigurationTabGroup
{
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTabGroup#createTabs(ILaunchConfigurationDialog, String)
	 */
	public void createTabs(ILaunchConfigurationDialog dialog, String mode)
	{
		/* Make sure the kernel module is loaded (just in case
		   the user has not authenticated or the module couldn't
		   be loaded). */
		if (!Oprofile.isKernelModuleLoaded()) {
			Oprofile.initializeOprofileModule();
		}

		ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[]
		{
			new CLaunchConfigurationTab(),
			new OprofileSetupTab(),
			new OprofileEventConfigTab(),
			new CommonTab()
		};
		setTabs(tabs);
	}
}
