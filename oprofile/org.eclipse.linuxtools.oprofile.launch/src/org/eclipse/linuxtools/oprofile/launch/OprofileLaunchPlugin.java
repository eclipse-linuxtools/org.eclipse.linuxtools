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
public class OprofileLaunchPlugin extends AbstractUIPlugin {
	//The shared instance.
	private static OprofileLaunchPlugin plugin;

	private static final String PLUGIN_ID = "org.eclipse.linuxtools.oprofile.launch";

	// The launch type ID for profiling
	public static final String ID_LAUNCH_PROFILE = "org.eclipse.linuxtools.oprofile.launch.oprofile"; //$NON-NLS-1$


	//Launch Configuration attributes
	
	// Global options \\
	public static final String ATTR_KERNEL_IMAGE_FILE = ID_LAUNCH_PROFILE + ".KERNEL_IMAGE"; 			//$NON-NLS-1$
	public static final String ATTR_SEPARATE_SAMPLES = ID_LAUNCH_PROFILE + ".SEPARATE_SAMPLES"; 		//$NON-NLS-1$
	public static final String ATTR_USE_DEFAULT_EVENT = ID_LAUNCH_PROFILE + ".USE_DEFAULT_EVENT";		//$NON-NLS-1$

	
	// Counter Attributes \\
	private static final String ATTR_COUNTER(int nr) { return ID_LAUNCH_PROFILE + ".COUNTER_" + nr; } 					//$NON-NLS-1$
	public static final String ATTR_COUNTER_ENABLED(int nr)  { return ATTR_COUNTER(nr) + ".ENABLED"; } 					//$NON-NLS-1$
	public static final String ATTR_COUNTER_EVENT(int nr) { return ATTR_COUNTER(nr)  + ".EVENT"; } 						//$NON-NLS-1$
	public static final String ATTR_COUNTER_PROFILE_KERNEL(int nr) { return ATTR_COUNTER(nr) + ".PROFILE_KERNEL"; }		//$NON-NLS-1$
	public static final String ATTR_COUNTER_PROFILE_USER(int nr) { return ATTR_COUNTER(nr) + ".PROFILE_USER"; } 		//$NON-NLS-1$
	public static final String ATTR_COUNTER_COUNT(int nr) { return ATTR_COUNTER(nr) + ".COUNT"; } 						//$NON-NLS-1$
	public static final String ATTR_COUNTER_UNIT_MASK(int nr) { return  ATTR_COUNTER(nr) + ".UNIT_MASK"; } 				//$NON-NLS-1$
	
	/**
	 * The constructor.
	 */
	public OprofileLaunchPlugin() {
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
	public static OprofileLaunchPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the workspace instance.
	 */
	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}
	
	public static String getUniqueIdentifier() {
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
