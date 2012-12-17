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
package org.eclipse.linuxtools.profiling.launch;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.cdt.launch.ui.CArgumentsTab;
import org.eclipse.cdt.launch.ui.CMainTab;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.EnvironmentTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.sourcelookup.SourceLookupTab;
import org.eclipse.linuxtools.internal.profiling.launch.provider.launch.ProviderFramework;

public abstract class ProfileLaunchConfigurationTabGroup extends AbstractLaunchConfigurationTabGroup {

	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		ArrayList<AbstractLaunchConfigurationTab> tabs = new ArrayList<AbstractLaunchConfigurationTab>();
		tabs.add(new CMainTab());
		tabs.add(new CArgumentsTab());
		
		tabs.addAll(Arrays.asList(getProfileTabs()));
		
		tabs.add(new EnvironmentTab());
		tabs.add(new SourceLookupTab());
		tabs.add(new CommonTab());
		
		setTabs(tabs.toArray(new AbstractLaunchConfigurationTab[tabs.size()]));
	}
	
	public abstract AbstractLaunchConfigurationTab[] getProfileTabs();

	/**
	 * Get all IDs of the specific type. This looks through extensions of
	 * the extension point <code>org.eclipse.linuxtools.profiling.launch.launchProvider</code>
	 * that have a specific type.
	 *
	 * @param type A profiling type (eg. memory, snapshot, timing, etc.)
	 * @return A <code>String []</code> of all IDs of the specific type.
	 * @since 1.1
	 * @deprecated
	 */
	@Deprecated
	public static String[] getTabGroupIdsForType(String type) {
		return ProviderFramework.getProviderIdsForType(type);
	}

}