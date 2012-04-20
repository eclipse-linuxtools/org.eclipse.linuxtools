/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.linuxtools.internal.valgrind.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.linuxtools.internal.valgrind.core.PluginConstants;
import org.eclipse.linuxtools.profiling.launch.ProfileLaunchShortcut;

public class ValgrindLaunchShortcut extends ProfileLaunchShortcut {


	@Override
	protected void setDefaultProfileAttributes(
			ILaunchConfigurationWorkingCopy wc) throws CoreException {
		ValgrindOptionsTab tab = new ValgrindOptionsTab();
		tab.setDefaults(wc);
		ILaunchConfigurationTab defaultTab = ValgrindLaunchPlugin.getDefault().getToolPage(PluginConstants.TOOL_EXT_DEFAULT);
		defaultTab.setDefaults(wc);
	}

	/**
	 * Method getValgrindLaunchConfigType.
	 * @return ILaunchConfigurationType
	 */
	@Override
	protected ILaunchConfigurationType getLaunchConfigType() {
		return getLaunchManager().getLaunchConfigurationType(ValgrindLaunchPlugin.LAUNCH_ID);
	}

}
