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
package org.eclipse.linuxtools.internal.profiling.provider.launch;

import java.util.Map;

import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.linuxtools.internal.profiling.provider.ProviderProfileConstants;
import org.eclipse.linuxtools.profiling.launch.ProfileLaunchConfigurationTabGroup;
import org.eclipse.linuxtools.profiling.launch.ProfileLaunchShortcut;

public class ProviderLaunchShortcut extends ProfileLaunchShortcut implements IExecutableExtension {

	// Profiling type.
	private String type;

	// Launch configuration type id.
	private String launchConfigId;

	public void setInitializationData(IConfigurationElement config,
			String propertyName, Object data) {
		Map<String, String> parameters = (Map<String, String>) data;
		String profilingType = parameters
				.get(ProviderProfileConstants.INIT_DATA_TYPE_KEY);
		String configId = parameters
				.get(ProviderProfileConstants.INIT_DATA_CONFIG_ID_KEY);

		if (profilingType == null) {
			profilingType = "";
		}
		if (configId == null) {
			configId = "";
		}

		setLaunchConfigID(configId);
		setProfilingType(profilingType);
	}

	@Override
	protected ILaunchConfigurationType getLaunchConfigType() {
		return getLaunchManager().getLaunchConfigurationType(getLaunchConfigID());
	}

	@Override
	protected ILaunchConfiguration findLaunchConfiguration(IBinary bin, String mode) {
		String type = getProfilingType();

		// check that there exists a provider for the given profiling type
		if (ProviderLaunchConfigurationDelegate.getProviderIdToRun(type) == null) {
			handleFail(Messages.ProviderLaunchShortcut_0 + " " + type);
			return null;
		}

		// create a launch configuration based on the shortcut
		ILaunchConfiguration config = createConfiguration(bin, false);
		boolean exists = false;

		try {
			for (ILaunchConfiguration cfg : getLaunchManager().getLaunchConfigurations()){
				if (areEqual(config, cfg)){
					exists = true;
				}
			}
		} catch (CoreException e) {
			exists = true;
		}

		// only save the configuration if it does not exist
		if (! exists) {
			createConfiguration(bin);
		}

		return super.findLaunchConfiguration(bin, mode);
	}

	/**
	 * @param cfg1 a launch configuration
	 * @param cfg2 a launch configuration
	 * @return true if the launch configurations contain the exact
	 * same attributes, and false otherwise.
	 */
	private boolean areEqual(ILaunchConfiguration cfg1,
			ILaunchConfiguration cfg2) {

		// We don't care about these attributes.
		final String BUILD_BEFORE_LAUNCH = "org.eclipse.cdt.launch.ATTR_BUILD_BEFORE_LAUNCH_ATTR";
		final String IN_CONSOLE = "org.eclipse.debug.ui.ATTR_CONSOLE_OUTPUT_ON";

		try {
			Map<?, ?> attrs1 = cfg1.getAttributes();
			Map<?, ?> attrs2 = cfg2.getAttributes();

			for (Object key1 : attrs1.keySet()) {
				if (! attrs2.containsKey(key1)
						&& ! key1.toString().equals(BUILD_BEFORE_LAUNCH)
						&& ! key1.toString().equals(IN_CONSOLE)) {
					return false;
				}
			}

			for (Object key2 : attrs2.keySet()) {
				if (! attrs1.containsKey(key2)
						&& ! key2.toString().equals(BUILD_BEFORE_LAUNCH)
						&& ! key2.toString().equals(IN_CONSOLE)) {
					return false;
				}
			}

			for (Object key1 : attrs1.keySet()) {
				for (Object key2 : attrs2.keySet()) {
					if (key1.toString().equals(key2.toString())
							&& ! attrs1.get(key1).toString().equals(attrs2.get(key2).toString())) {
						return false;
					}
				}
			}
		} catch (CoreException e) {
			return false;
		}

		return true;
	}


	@Override
	protected void setDefaultProfileAttributes(ILaunchConfigurationWorkingCopy wc) {

		// acquire a provider id to run.
		String providerId = ProviderLaunchConfigurationDelegate.getProviderIdToRun(getProfilingType());

		// get tab group associated with provider id.
		ProfileLaunchConfigurationTabGroup tabgroup = ProfileLaunchConfigurationTabGroup.getTabGroupProviderFromId(providerId);

		/**
		 * Certain tabs' setDefaults(ILaunchConfigurationWorkingCopy) may
		 * require a launch configuration dialog. Eg. CMainTab generates
		 * a name for the configuration based on generateName in setDefaults()
		 * so we can create a temporary launch configuration dialog here. With
		 * the exception of generateName(String), the other methods do not
		 * get called.
		 */
		ILaunchConfigurationDialog dialog  = new ILaunchConfigurationDialog() {

			public void run(boolean fork, boolean cancelable,
					IRunnableWithProgress runnable) {
				throw new UnsupportedOperationException ();
			}

			public void updateMessage() {
				throw new UnsupportedOperationException ();
			}

			public void updateButtons() {
				throw new UnsupportedOperationException ();
			}

			public void setName(String name) {
				throw new UnsupportedOperationException ();
			}

			public void setActiveTab(int index) {
				throw new UnsupportedOperationException ();
			}

			public void setActiveTab(ILaunchConfigurationTab tab) {
				throw new UnsupportedOperationException ();
			}

			public ILaunchConfigurationTab[] getTabs() {
				return null;
			}

			public String getMode() {
				return null;
			}

			public ILaunchConfigurationTab getActiveTab() {
				return null;
			}

			public String generateName(String name) {
				if (name == null) {
					name = "";
				}
				return getLaunchManager().generateLaunchConfigurationName(name);
			}
		};

		tabgroup.createTabs(dialog , "profile"); //$NON-NLS-1$

		// set configuration to match default attributes for all tabs.
		for (ILaunchConfigurationTab tab : tabgroup.getTabs()) {
			tab.setLaunchConfigurationDialog(dialog);
			tab.setDefaults(wc);
		}

		// get configuration shortcut associated with provider id.
		ProfileLaunchShortcut shortcut= ProfileLaunchShortcut.getLaunchShortcutProviderFromId(providerId);
		// set attributes related to the specific profiling shortcut configuration.
		shortcut.setDefaultProfileLaunchShortcutAttributes(wc);

		wc.setAttribute(ProviderProfileConstants.PROVIDER_CONFIG_ATT,
				providerId);
	}

	/**
	 * Get name of profiling type that used for this tab.
	 *
	 * @return String profiling name.
	 */
	private void setProfilingType(String profilingType) {
		type = profilingType;
	}
	/**
	 * Set launch configuration type id.
	 *
	 * @param configId String configuration type id.
	 */
	private void setLaunchConfigID(String configId) {
		launchConfigId = configId;
	}

	/**
	 * Get profiling type of this plug-in.
	 *
	 * @return String profiling type this plug-in supports.
	 */
	private String getLaunchConfigID() {
		return launchConfigId;
	}

	public String getProfilingType() {
		return type;
	}

}
