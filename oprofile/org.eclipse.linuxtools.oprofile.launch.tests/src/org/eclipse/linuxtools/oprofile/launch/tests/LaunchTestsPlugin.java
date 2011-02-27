/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kent Sebastian <ksebasti@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.oprofile.launch.tests;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class LaunchTestsPlugin extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.linuxtools.oprofile.launch.tests"; //$NON-NLS-1$

	// The shared instance
	private static LaunchTestsPlugin plugin;
	
	/**
	 *  Java system properties.
	 *  usage: -Dorg.eclipse.linuxtools.oprofile.launch.tests.runOprofile=<yes|no> [default: yes]
	 *  if yes, will run the launch tests
	 *     no, will skip the launch tests (they all require oprofile to be set up)
	 */
	public static final String SYSTEM_PROPERTY_RUN_OPROFILE = "org.eclipse.linuxtools.oprofile.launch.tests.runOprofile"; //$NON-NLS-1$
	public static final boolean RUN_OPROFILE = System.getProperty(SYSTEM_PROPERTY_RUN_OPROFILE, "yes").equals("yes"); //$NON-NLS-1$ //$NON-NLS-2$

	
	/**
	 * The constructor
	 */
	public LaunchTestsPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static LaunchTestsPlugin getDefault() {
		return plugin;
	}

}
