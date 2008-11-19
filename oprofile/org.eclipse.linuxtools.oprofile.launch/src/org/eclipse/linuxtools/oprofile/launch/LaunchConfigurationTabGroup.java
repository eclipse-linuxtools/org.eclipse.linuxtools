/*******************************************************************************
 * Copyright (c) 2004,2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Keith Seitz <keiths@redhat.com> - initial API and implementation
 *    Kent Sebastian <ksebasti@redhat.com> - 
 *******************************************************************************/ 
package org.eclipse.linuxtools.oprofile.launch;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.linuxtools.oprofile.core.Oprofile;

/**
 * This class defines the launch tab group that is displayed by the
 * launch manager.
 */
public class LaunchConfigurationTabGroup
	extends AbstractLaunchConfigurationTabGroup
{
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTabGroup#createTabs(ILaunchConfigurationDialog, String)
	 */
	public void createTabs(ILaunchConfigurationDialog dialog, String mode)
	{
//really not needed here		
//		/* Make sure the kernel module is loaded (just in case
//		   the user has not authenticated or the module couldn't
//		   be loaded). */
//		if (!Oprofile.isKernelModuleLoaded()) {
//			Oprofile.initializeOprofileModule();
//		}

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
