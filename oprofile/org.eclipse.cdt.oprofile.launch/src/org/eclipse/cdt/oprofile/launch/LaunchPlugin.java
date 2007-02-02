/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/

package org.eclipse.cdt.oprofile.launch;

import org.eclipse.ui.plugin.*;
import org.eclipse.core.resources.*;
import java.util.*;

/**
 * The main plugin class to be used in the desktop.
 * @author keiths
 */
public class LaunchPlugin extends AbstractUIPlugin {
	//The shared instance.
	private static LaunchPlugin plugin;
	//Resource bundle.
	private ResourceBundle resourceBundle;

	private static final String PLUGIN_ID = "org.eclipse.cdt.oprofile.launch";

	// The launch type ID for profiling
	public static final String ID_LAUNCH_PROFILE = "org.eclipse.cdt.oprofile.launch.oprofile"; //$NON-NLS-1$

	/*
	 *  LaunchConfiguration attributes
	 */
	 
	// C LaunchConfiguration to profile
	public static final String ATTR_C_LAUNCH_CONFIG = ID_LAUNCH_PROFILE + ".C_LAUNCH_CONFIG"; //$NON-NLS-1$
	
	// Global options \\
	
	// Kernel image file
	public static final String ATTR_KERNEL_IMAGE_FILE = ID_LAUNCH_PROFILE + ".KERNEL_IMAGE"; //$NON-NLS-1$
	
	// Hash table size
	// DEPRECATED public static final String ATTR_HASHTABLE_SIZE = ID_LAUNCH_PROFILE + ".HASHTABLE_SIZE"; //$NON-NLS-1$
	
	// Buffer size
	public static final String ATTR_BUFFER_SIZE = ID_LAUNCH_PROFILE + ".BUFFER_SIZE"; //$NON-NLS-1$
	
	// Process ID filter
	//public static final String ATTR_PROCESS_ID_FILTER = ID_LAUNCH_PROFILE + ".PROCESS_ID_FILTER";
	
	// Process group filter
	//public static final String ATTR_PROCESS_GROUP_FILTER = ID_LAUNCH_PROFILE + ".PROCESS_GRP_FILTER";
	
	// Note size
	// DEPRECATED public static final String ATTR_NOTE_SIZE = ID_LAUNCH_PROFILE + ".NOTE_SIZE"; //$NON-NLS-1$
	
	// Kernel only
	// DEPRECATED public static final String ATTR_KERNEL_ONLY = ID_LAUNCH_PROFILE + ".KERNEL_ONLY"; //$NON-NLS-1$
	
	// Verbose daemon logging
	public static final String ATTR_VERBOSE_LOGGING = ID_LAUNCH_PROFILE + ".VERBOSE_LOGGING"; //$NON-NLS-1$

	// Profile daemon?
	// DEPRECATED public static final String ATTR_OMIT_DAEMON = ID_LAUNCH_PROFILE + ".OMIT_DAEMON"; //$NON-NLS-1$
		
	// Separate samples
	public static final String ATTR_SEPARATE_SAMPLES = ID_LAUNCH_PROFILE + ".SEPARATE_SAMPLES"; //$NON-NLS-1$
	
	// Counter Attributes \\
	
	// Counters
	private static final String ATTR_COUNTER(int nr) { return ID_LAUNCH_PROFILE + ".COUNTER_" + nr; } //$NON-NLS-1$
	
	// Enabled?
	public static final String ATTR_COUNTER_ENABLED(int nr)  { return ATTR_COUNTER(nr) + ".ENABLED"; } //$NON-NLS-1$

	// Event to monitor
	public static final String ATTR_COUNTER_EVENT(int nr) { return ATTR_COUNTER(nr)  + ".EVENT"; } //$NON-NLS-1$
	
	// Profile kernel?
	public static final String ATTR_COUNTER_PROFILE_KERNEL(int nr) { return ATTR_COUNTER(nr) + ".PROFILE_KERNEL"; } //$NON-NLS-1$

	// Profile user binaries?
	public static final String ATTR_COUNTER_PROFILE_USER(int nr) { return ATTR_COUNTER(nr) + ".PROFILE_USER"; } //$NON-NLS-1$
	
	// Count
	public static final String ATTR_COUNTER_COUNT(int nr) { return ATTR_COUNTER(nr) + ".COUNT"; } //$NON-NLS-1$
	
	// Unit mask
	public static final String ATTR_COUNTER_UNIT_MASK(int nr) { return  ATTR_COUNTER(nr) + ".UNIT_MASK"; } //$NON-NLS-1$
	
	/**
	 * The constructor.
	 */
	public LaunchPlugin() {
		plugin = this;
		try {
			resourceBundle= ResourceBundle.getBundle("org.eclipse.cdt.oprofile.launch.LaunchPluginResources"); //$NON-NLS-1$
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
	}

	/**
	 * Returns the shared instance.
	 */
	public static LaunchPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the workspace instance.
	 */
	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle= LaunchPlugin.getDefault().getResourceBundle();
		try {
			return bundle.getString(key);
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}
	
	public static String getUniqueIdentifier()
	{
		return PLUGIN_ID;
	}
}
