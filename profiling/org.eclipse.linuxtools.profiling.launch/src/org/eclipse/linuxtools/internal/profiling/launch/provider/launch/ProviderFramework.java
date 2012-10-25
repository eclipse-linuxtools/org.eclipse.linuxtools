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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.linuxtools.internal.profiling.launch.ProfileLaunchPlugin;
import org.eclipse.linuxtools.profiling.launch.ProfileLaunchConfigurationDelegate;
import org.eclipse.linuxtools.profiling.launch.ProfileLaunchShortcut;

/**
 * This class has various methods to access relevant information from
 * the extension point defined by the schema :
 *
 * org.eclipse.linuxtools.profiling.launch.launchProvider
 *
 */
public class ProviderFramework {

	/**
	 * Get a profiling launch shortcut that provides the specified type of profiling. This
	 * looks through extensions of the extension point
	 * <code>org.eclipse.linuxtools.profiling.launch.launchProvider</code> that have a
	 * specific type attribute.
	 *
	 * @param type A profiling type (eg. memory, snapshot, timing, etc.)
	 * @return a profiling launch shortcut that implements <code>ProfileLaunchShortcut</code>
	 * and provides the necessary profiling type, or <code>null</code> if none could be found.
	 * @since 1.2
	 */
	public static ProfileLaunchShortcut getProfilingProvider(String type) {
		ArrayList<IConfigurationElement> configList = getOrderedConfigElements(type);

		for (IConfigurationElement config : configList) {
			try {
				Object obj = config.createExecutableExtension("shortcut"); //$NON-NLS-1$
				if (obj instanceof ProfileLaunchShortcut) {
					return (ProfileLaunchShortcut) obj;
				}
			} catch (CoreException e) {
				// continue, other configuration may succeed
			}
		}
		return null;
	}

	/**
	 * Get a profiling launch shortcut that is associated with the specified id.
	 * This looks through extensions of the extension point
	 * <code>org.eclipse.linuxtools.profiling.launch.launchProvider</code>
	 * that have a specific id.
	 *
	 * @param id A unique identifier
	 * @return a profiling launch shortcut that implements <code>ProfileLaunchShortcut</code>
	 * and provides the necessary profiling type, or <code>null</code> if none could be found.
	 * @since 1.2
	 */
	public static ProfileLaunchShortcut getLaunchShortcutProviderFromId(
			String id) {
		IConfigurationElement[] configs = getConfigurationElements();
		for (IConfigurationElement config : configs) {
			if (config.getName().equals("provider")) { //$NON-NLS-1$
				String currentId = config.getAttribute("id"); //$NON-NLS-1$
				String shortcut = config.getAttribute("shortcut"); //$NON-NLS-1$
				if (currentId != null && shortcut != null
						&& currentId.equals(id)) {
					try {
						Object obj = config
								.createExecutableExtension("shortcut"); //$NON-NLS-1$
						if (obj instanceof ProfileLaunchShortcut) {
							return (ProfileLaunchShortcut) obj;
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
	 * Get a launch configuration delegate that is associated with the specified id.
	 * This looks through extensions of the extension point
	 * <code>org.eclipse.linuxtools.profiling.launch.launchProvider</code> that
	 * have a specific delegate attribute.
	 *
	 * @param id a unique identifier
	 * @return a launch configuration delegate that implements
	 * <code>ProfileLaunchConfigurationDelegate</code> , or <code>null</code> if
	 * none could be found.
	 * @since 1.2
	 */
	public static ProfileLaunchConfigurationDelegate getConfigurationDelegateFromId(
			String id) {
		IConfigurationElement[] configs = getConfigurationElements();
		for (IConfigurationElement config : configs) {
			if (config.getName().equals("provider")) { //$NON-NLS-1$
				String currentId = config.getAttribute("id"); //$NON-NLS-1$
				String tabgroup = config.getAttribute("delegate"); //$NON-NLS-1$
				if (currentId != null && tabgroup != null
						&& currentId.equals(id)) {
					try {
						Object obj = config
								.createExecutableExtension("delegate"); //$NON-NLS-1$
						if (obj instanceof ProfileLaunchConfigurationDelegate) {
							return (ProfileLaunchConfigurationDelegate) obj;
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
		ArrayList<IConfigurationElement> list = getOrderedConfigElements(type);

		if (list.size() > 0) {
			return list.get(0).getAttribute("id"); //$NON-NLS-1$
		}
		return null;
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
		IConfigurationElement[] configs = getConfigurationElements();
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

	/**
	 * Get launch providers for a given type and order them with regards to highest priority first.
	 *
	 * @param type
	 * @return array of launch provider configuration elements in prioritized order
	 * @since 1.2
	 */
	public static ArrayList<IConfigurationElement> getOrderedConfigElements(String type) {
		IConfigurationElement[] configs = getConfigurationElements();
		ArrayList<IConfigurationElement> configList = new ArrayList<IConfigurationElement>();

		for (IConfigurationElement config : configs) {
			if (config.getName().equals("provider")) { //$NON-NLS-1$
				String currentType = config.getAttribute("type"); //$NON-NLS-1$
				if (currentType != null && currentType.equals(type)) {

					String priority = config.getAttribute("priority"); //$NON-NLS-1$
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
					p1 = Integer.parseInt(c1.getAttribute("priority")); //$NON-NLS-1$
					if (p1 <= 0) {
						return 1;
					}
				} catch (NumberFormatException e) {
					return 1;
				}
				try {
					p2 = Integer.parseInt(c2.getAttribute("priority")); //$NON-NLS-1$
					if (p2 <= 0) {
						return -1;
					}
				} catch (NumberFormatException e) {
					return -1;
				}
				return p1 < p2 ? -1 : 1;
			}
		});
		return configList;
	}

	/**
	 * Helper method to return the list of extensions that contribute the the
	 * provider framework.
	 * @return All extensions that contribute to the provider framework.
	 */
	private static IConfigurationElement [] getConfigurationElements () {
		IExtensionPoint extPoint = Platform.getExtensionRegistry()
				.getExtensionPoint(ProfileLaunchPlugin.PLUGIN_ID, "launchProvider"); //$NON-NLS-1$
		return extPoint.getConfigurationElements();
	}

}
