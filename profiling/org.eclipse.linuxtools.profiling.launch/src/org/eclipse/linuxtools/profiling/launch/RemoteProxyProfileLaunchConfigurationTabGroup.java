/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *    Rafael Teixeira <rafaelmt@linux.vnet.ibm.com> - Switched to
 * 	RemoteProxyCMainTab
 *******************************************************************************/

package org.eclipse.linuxtools.profiling.launch;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.cdt.launch.ui.CArgumentsTab;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.EnvironmentTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.sourcelookup.SourceLookupTab;

/**
 * @since 1.1
 */
public abstract class RemoteProxyProfileLaunchConfigurationTabGroup extends AbstractLaunchConfigurationTabGroup {

	@Override
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		ArrayList<AbstractLaunchConfigurationTab> tabs = new ArrayList<>();
		tabs.add(new RemoteProxyCMainTab());
		tabs.add(new CArgumentsTab());

		tabs.addAll(Arrays.asList(getProfileTabs()));

		tabs.add(new EnvironmentTab());
		tabs.add(new SourceLookupTab());
		tabs.add(new CommonTab());

		setTabs(tabs.toArray(new AbstractLaunchConfigurationTab[tabs.size()]));
	}

	public abstract AbstractLaunchConfigurationTab[] getProfileTabs();

}
