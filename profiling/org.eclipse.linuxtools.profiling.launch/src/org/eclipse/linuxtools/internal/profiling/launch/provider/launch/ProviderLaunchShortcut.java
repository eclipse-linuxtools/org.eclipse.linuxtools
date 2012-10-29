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
package org.eclipse.linuxtools.internal.profiling.launch.provider.launch;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.linuxtools.internal.profiling.launch.provider.ProviderProfileConstants;
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
		if (ProviderLaunchConfigurationDelegate.getProviderIdToRun(null, type) == null) {
			handleFail(Messages.ProviderLaunchShortcut_0 + " " + type);
			return null;
		}

		// create a launch configuration based on the shortcut
		ILaunchConfiguration config = createConfiguration(bin, false);
		boolean exists = false;

		try {
			for (ILaunchConfiguration cfg : getLaunchManager().getLaunchConfigurations(getLaunchConfigType())){
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
		final List<String> IGNORED_ATTRS = Arrays.asList(new String [] {
						ICDTLaunchConfigurationConstants.ATTR_BUILD_BEFORE_LAUNCH,
						ICDTLaunchConfigurationConstants.ATTR_COREFILE_PATH,
						IDebugUIConstants.ATTR_CAPTURE_IN_CONSOLE });

		try {
			Map<?, ?> attrs1 = cfg1.getAttributes();
			Map<?, ?> attrs2 = cfg2.getAttributes();

			for (Object key1 : attrs1.keySet()) {
				if (! attrs2.containsKey(key1)
						&& ! IGNORED_ATTRS.contains(key1.toString())) {
					return false;
				}
			}

			for (Object key2 : attrs2.keySet()) {
				if (! attrs1.containsKey(key2)
						&& ! IGNORED_ATTRS.contains(key2.toString())) {
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
		final String providerId = ProviderLaunchConfigurationDelegate.getProviderIdToRun(wc, getProfilingType());

		// get tool name from id.
		final String providerToolName = ProviderFramework.getProviderToolNameFromId(providerId);

		// get tab group associated with provider id.
		final ProfileLaunchConfigurationTabGroup tabgroup = ProfileLaunchConfigurationTabGroup.getTabGroupProviderFromId(providerId);

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
				String providerConfigutationName = generateProviderConfigurationName(name, providerToolName);
				return getLaunchManager().generateLaunchConfigurationName(providerConfigutationName);
			}
		};

		tabgroup.createTabs(dialog , "profile"); //$NON-NLS-1$

		// set configuration to match default attributes for all tabs.
		for (ILaunchConfigurationTab tab : tabgroup.getTabs()) {
			tab.setLaunchConfigurationDialog(dialog);
			tab.setDefaults(wc);
		}

		// get configuration shortcut associated with provider id.
		ProfileLaunchShortcut shortcut= ProviderFramework.getLaunchShortcutProviderFromId(providerId);
		// set attributes related to the specific profiling shortcut configuration.
		shortcut.setDefaultProfileLaunchShortcutAttributes(wc);

		wc.setAttribute(ProviderProfileConstants.PROVIDER_CONFIG_ATT,
				providerId);

		// set tool name in configuration.
		wc.setAttribute(ProviderProfileConstants.PROVIDER_CONFIG_TOOLNAME_ATT, providerToolName);

		/**
		 * To avoid renaming an already renamed launch configuration, we can
		 * check the expected format of the name using regular expressions and
		 * skip on matches.
		 */
		String curConfigName = wc.getName();
		Pattern configNamePattern = Pattern.compile(".+\\s\\[.+\\](\\s\\(\\d+\\))?$"); //$NON-NLS-1$
		Matcher match = configNamePattern.matcher(curConfigName);
		if (!match.find()) {
			wc.rename(dialog.generateName(curConfigName));
		}
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

	/**
	 * Generate a string that can be used as a name for a provider launch configuration.
	 * It combines <code>configName</code> and <code>toolName</code> into a String of
	 * consistent format: <configuration name> [<tool name>].
	 *
	 * @param configName
	 * @param toolName
	 * @return String tool name appended to original configuration name.
	 * @since 1.2
	 */
	public static String generateProviderConfigurationName(String configName, String toolName){
		return configName + " " + "[" + toolName + "]"; //$NON-NLS-1$
	}

}
