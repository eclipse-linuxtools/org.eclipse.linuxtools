/*******************************************************************************
 * Copyright (c) 2012 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat Inc. - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.linuxtools.gprof.launch;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;


/**
 * The main plugin class to be used in the desktop.
 */
public class GprofLaunchPlugin extends AbstractUIPlugin {
	//The shared instance.
	private static GprofLaunchPlugin plugin;
	//shared cache instance for configuration

	private static final String PLUGIN_ID = "org.eclipse.linuxtools.gprof.launch"; //$NON-NLS-1$
	public static final String ID_GCOV_VIEW = "org.eclipse.linuxtools.gcov.view"; //$NON-NLS-1$
	public static final String LAUNCH_ID = PLUGIN_ID + ".gcovLaunch"; //$NON-NLS-1$

	/**
	 * The constructor.
	 */
	public GprofLaunchPlugin() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}	

	public static GprofLaunchPlugin getDefault() {
		return plugin;
	}

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
