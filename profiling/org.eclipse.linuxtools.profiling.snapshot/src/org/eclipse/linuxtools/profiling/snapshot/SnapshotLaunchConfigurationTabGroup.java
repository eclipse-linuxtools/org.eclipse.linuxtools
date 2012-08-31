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
package org.eclipse.linuxtools.profiling.snapshot;

import java.util.ArrayList;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.linuxtools.internal.profiling.provider.ProviderLaunchConfigurationTabGroup;

/**
 * The tab group for this plug-in's launch configuration.
 * 
 */
public class SnapshotLaunchConfigurationTabGroup extends
		ProviderLaunchConfigurationTabGroup {

	@Override
	public AbstractLaunchConfigurationTab[] getProfileTabs() {
		ArrayList<AbstractLaunchConfigurationTab> tabs = new ArrayList<AbstractLaunchConfigurationTab>();
		tabs.add(new SnapshotOptionsTab());

		return tabs.toArray(new AbstractLaunchConfigurationTab [] {});
	}

}
