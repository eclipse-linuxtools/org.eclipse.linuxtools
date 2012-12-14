/*******************************************************************************
 * (C) Copyright 2012 IBM Corp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Daniel H Barboza (IBM) - Initial implementation.
 *******************************************************************************/

package org.eclipse.linuxtools.internal.profiling.launch.provider.remote;

import java.util.ArrayList;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.linuxtools.internal.profiling.launch.provider.remote.ProviderProfileConstants;
import org.eclipse.linuxtools.profiling.launch.RemoteProxyProfileLaunchConfigurationTabGroup;

public class ProviderLaunchConfigurationTabGroup extends 
RemoteProxyProfileLaunchConfigurationTabGroup implements IExecutableExtension {

	// Profiling type.
	private String type;

	// Profiling type name to be displayed.
	private String name;

	public void setInitializationData(IConfigurationElement config,
			String propertyName, Object data) {
		Map<String, String> parameters = (Map<String, String>) data;
		String profilingType = parameters
				.get(ProviderProfileConstants.INIT_DATA_TYPE_KEY);
		String profilingTypeName = parameters
				.get(ProviderProfileConstants.INIT_DATA_NAME_KEY);

		if (profilingType == null) {
			profilingType = "";
		}
		if (profilingTypeName == null) {
			profilingTypeName = "";
		}

		setProfilingType(profilingType);
		setProfilingTypeName(profilingTypeName);
	}

	@Override
	public AbstractLaunchConfigurationTab[] getProfileTabs() {
		ArrayList<AbstractLaunchConfigurationTab> tabs = new ArrayList<AbstractLaunchConfigurationTab>();
		tabs.add(new ProviderOptionsTab(type, name));

		return tabs.toArray(new AbstractLaunchConfigurationTab [] {});
	}

	/**
	 * Set profiling type.
	 *
	 * @param profilingType
	 */
	private void setProfilingType(String profilingType) {
		type = profilingType;
	}

	/**
	 * Set profiling type name to be displayed.
	 *
	 * @param profilingTypeName
	 */
	private void setProfilingTypeName(String profilingTypeName) {
		name = profilingTypeName;
	}
}
