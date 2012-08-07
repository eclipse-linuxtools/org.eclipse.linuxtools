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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.eclipse.cdt.launch.ui.CArgumentsTab;
import org.eclipse.cdt.launch.ui.CMainTab;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.EnvironmentTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.sourcelookup.SourceLookupTab;
import org.eclipse.linuxtools.internal.profiling.launch.ProfileLaunchPlugin;

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
	 * Get a profiling tab that provides the specified type of profiling. This
	 * looks through extensions of the extension point
	 * <code>org.eclipse.linuxtools.profiling.launch.launchProvider</code> that have a
	 * specific type attribute.
	 *
	 * @param type A profiling type (eg. memory, snapshot, timing, etc.)
	 * @return a tab that implements <code>ProfileLaunchConfigurationTabGroup</code>
	 * and provides the necessary profiling type, or <code>null</code> if none could be found.
	 * @since 1.1
	 */
	public static ProfileLaunchConfigurationTabGroup getTabGroupProvider(String type) {
		IExtensionPoint extPoint = Platform.getExtensionRegistry()
				.getExtensionPoint(ProfileLaunchPlugin.PLUGIN_ID,
						"launchProvider"); //$NON-NLS-1$
		IConfigurationElement[] configs = extPoint.getConfigurationElements();
		for (IConfigurationElement config : configs) {
			if (config.getName().equals("provider")) { //$NON-NLS-1$
				String currentType = config.getAttribute("type"); //$NON-NLS-1$
				String shortcut = config.getAttribute("tabgroup"); //$NON-NLS-1$
				if (currentType != null && shortcut != null
						&& currentType.equals(type)) {
					try {
						Object obj = config
								.createExecutableExtension("tabgroup"); //$NON-NLS-1$
						if (obj instanceof ProfileLaunchConfigurationTabGroup) {
							return (ProfileLaunchConfigurationTabGroup) obj;
						}
					} catch (CoreException e) {
						// continue, perhaps another configuration will succeed
					}
				}
			}
		}
		return null;
	}

	/**
	 * Get a profiling tab that is associated with the specified id.
	 * This looks through extensions of the extension point
	 * <code>org.eclipse.linuxtools.profiling.launch.launchProvider</code> that have a
	 * specific id.
	 *
	 * @param id A unique identifier
	 * @return a tab that implements <code>ProfileLaunchConfigurationTabGroup</code>
	 * and provides the necessary profiling type, or <code>null</code> if none could be found.
	 * @since 1.1
	 */
	public static ProfileLaunchConfigurationTabGroup getTabGroupProviderFromId(
			String id) {
		IExtensionPoint extPoint = Platform.getExtensionRegistry()
				.getExtensionPoint(ProfileLaunchPlugin.PLUGIN_ID,
						"launchProvider"); //$NON-NLS-1$
		IConfigurationElement[] configs = extPoint.getConfigurationElements();
		for (IConfigurationElement config : configs) {
			if (config.getName().equals("provider")) { //$NON-NLS-1$
				String currentId = config.getAttribute("id"); //$NON-NLS-1$
				String tabgroup = config.getAttribute("tabgroup"); //$NON-NLS-1$
				if (currentId != null && tabgroup != null
						&& currentId.equals(id)) {
					try {
						Object obj = config
								.createExecutableExtension("tabgroup"); //$NON-NLS-1$
						if (obj instanceof ProfileLaunchConfigurationTabGroup) {
							return (ProfileLaunchConfigurationTabGroup) obj;
						}
					} catch (CoreException e) {
						// continue, perhaps another configuration will succeed
					}
				}
			}
		}
		return null;
	}
	/**
	 * Get id of highest priority profiling tabgroup launch configuration that
	 * provides the type of profiling. This looks through extensions of the
	 * <code>org.eclipse.linuxtools.profiling.launch.launchProvider</code>
	 * extension point that have a specific type attribute.
	 *
	 * @param type A profiling type (eg. memory, snapshot, timing, etc.)
	 * @return an id of the profiling launch shortcut that implements
	 * <code>ProfileLaunchShortcut</code> and provides the necessary
	 * profiling type, or <code>null</code> if none could be found.
	 * @since 1.2
	 */
	public static String getHighestProviderId(String type) {
		IExtensionPoint extPoint = Platform.getExtensionRegistry()
				.getExtensionPoint(ProfileLaunchPlugin.PLUGIN_ID,
						"launchProvider"); //$NON-NLS-1$
		IConfigurationElement[] configs = extPoint.getConfigurationElements();
		ArrayList<IConfigurationElement> configList = new ArrayList<IConfigurationElement>();

		for (IConfigurationElement config : configs) {
			if (config.getName().equals("provider")) { //$NON-NLS-1$
				String currentName = config.getAttribute("name"); //$NON-NLS-1$
				String currentType = config.getAttribute("type"); //$NON-NLS-1$
				String tabgroup = config.getAttribute("tabgroup"); //$NON-NLS-1$
				if (currentType != null && tabgroup != null
						&& currentName != null && currentType.equals(type)) {

					String priority = config.getAttribute("priority");
					if (priority != null) {
						try {
							Integer.parseInt(priority);
							configList.add(config);
						} catch (NumberFormatException e) {
							// continue
						}
					}
				}
			}
		}

		Collections.sort(configList, new Comparator<IConfigurationElement>() {
			public int compare(IConfigurationElement c1,
					IConfigurationElement c2) {
				int p1, p2;
				// If priority is not an int or is < 0, corresponding config has
				// lowest priority.
				try {
					p1 = Integer.parseInt(c1.getAttribute("priority"));
					if (p1 <= 0) {
						return 1;
					}
				} catch (NumberFormatException e) {
					return 1;
				}
				try {
					p2 = Integer.parseInt(c2.getAttribute("priority"));
					if (p2 <= 0) {
						return -1;
					}
				} catch (NumberFormatException e) {
					return -1;
				}
				return p1 < p2 ? -1 : 1;
			}
		});

		for (IConfigurationElement config : configList) {
			try {
				Object obj = config.createExecutableExtension("tabgroup"); //$NON-NLS-1$
				if (obj instanceof ProfileLaunchConfigurationTabGroup) {
					return config.getAttribute("id");
				}
			} catch (CoreException e) {
				// continue, other configuration may succeed
			}
		}
		return null;
	}

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
		return getProviderIdsForType(type);
	}

	/**
	 * Get all IDs of the specific type. This looks through extensions of
	 * the extension point <code>org.eclipse.linuxtools.profiling.launch.launchProvider</code>
	 * that have a specific type.
	 *
	 * @param type A profiling type (eg. memory, snapshot, timing, etc.)
	 * @return A <code>String []</code> of all IDs of the specific type.
	 * @since 1.2
	 */
	public static String[] getProviderIdsForType(String type) {
		ArrayList<String> ret = new ArrayList<String> ();
		IExtensionPoint extPoint = Platform.getExtensionRegistry()
				.getExtensionPoint(ProfileLaunchPlugin.PLUGIN_ID,
						"launchProvider"); //$NON-NLS-1$
		IConfigurationElement[] configs = extPoint.getConfigurationElements();
		for (IConfigurationElement config : configs) {
			if (config.getName().equals("provider")) { //$NON-NLS-1$
				String currentId = config.getAttribute("id"); //$NON-NLS-1$
				String currentType = config.getAttribute("type"); //$NON-NLS-1$
				if (currentType != null && type != null
						&& currentType.equals(type)) {
					ret.add(currentId);
				}
			}
		}
		return ret.toArray(new String [] {});
	}

	/**
	 * Get map of all pairs of names and IDs of the specific provider type. This
	 * looks through extensions of the extension point
	 * <code>org.eclipse.linuxtools.profiling.launch.launchProvider</code>
	 * that have a specific type.
	 *
	 * @param type A profiling type (eg. memory, snapshot, timing, etc.)
	 * @return A <code>HashMap<String, String></code> of all pairs of names and IDs
	 * of the specific type.
	 * @since 1.2
	 */
	public static HashMap<String, String> getProviderNamesForType(String type) {
		HashMap<String, String> ret = new HashMap<String, String>();
		IExtensionPoint extPoint = Platform.getExtensionRegistry()
				.getExtensionPoint(ProfileLaunchPlugin.PLUGIN_ID,
						"launchProvider"); //$NON-NLS-1$
		IConfigurationElement[] configs = extPoint.getConfigurationElements();
		for (IConfigurationElement config : configs) {
			if (config.getName().equals("provider")) { //$NON-NLS-1$
				String currentId = config.getAttribute("id"); //$NON-NLS-1$
				String currentName = config.getAttribute("name"); //$NON-NLS-1$
				String currentType = config.getAttribute("type"); //$NON-NLS-1$
				if (currentType != null && type != null
						&& currentType.equals(type) && currentName != null) {
					ret.put(currentName, currentId);
				}
			}
		}
		return ret;
	}
}