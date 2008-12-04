/*******************************************************************************
 * Copyright (c) 2004 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Keith Seitz <keiths@redhat.com> - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.linuxtools.oprofile.launch;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.*;
import org.eclipse.core.resources.*;
import org.osgi.framework.BundleContext;


/**
 * The main plugin class to be used in the desktop.
 */
public class LaunchPlugin extends AbstractUIPlugin {
	//The shared instance.
	private static LaunchPlugin plugin;

	private static final String PLUGIN_ID = "org.eclipse.linuxtools.oprofile.launch";

	// The launch type ID for profiling
	public static final String ID_LAUNCH_PROFILE = "org.eclipse.linuxtools.oprofile.launch.oprofile"; //$NON-NLS-1$

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
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
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
	
	public static String getUniqueIdentifier()
	{
		return PLUGIN_ID;
	}
	
	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		return getDefault().getWorkbench().getActiveWorkbenchWindow();
	}
	
	public static Shell getActiveWorkbenchShell() {
		IWorkbenchWindow window = getActiveWorkbenchWindow();
		if (window != null) {
			return window.getShell();
		}
		return null;
	}	
	
}
