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
package org.eclipse.linuxtools.profiling.memory;

import java.util.ArrayList;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.linuxtools.profiling.launch.ProfileLaunchConfigurationTabGroup;

/**
 * 
 * Special version of ProfileLaunchConfigurationTabGroup that accesses the tab group from
 * the chosen memory profiling provider.
 *
 */
public class MemoryLaunchConfigurationTabGroup extends
		ProfileLaunchConfigurationTabGroup {

	@Override
	public AbstractLaunchConfigurationTab[] getProfileTabs() {
		ArrayList<AbstractLaunchConfigurationTab> tabs = new ArrayList<AbstractLaunchConfigurationTab>();
		tabs.add(new MemoryOptionsTab());

		return tabs.toArray(new AbstractLaunchConfigurationTab [] {});
	}

}
