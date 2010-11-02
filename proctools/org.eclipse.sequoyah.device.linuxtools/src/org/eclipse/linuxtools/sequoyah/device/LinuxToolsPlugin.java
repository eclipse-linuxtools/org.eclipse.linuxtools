/********************************************************************************
 * Copyright (c) 2008 Motorola Inc. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributor:
 * Otavio Ferranti (Motorola)
 *
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.linuxtools.sequoyah.device;

import org.eclipse.sequoyah.device.common.utilities.BasePlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class LinuxToolsPlugin extends BasePlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.linuxtools.sequoyah.device"; //$NON-NLS-1$

	// Common icons
	public static final String ICON_RUN = "run.gif"; //$NON-NLS-1$
	public static final String ICON_PAUSE = "pause.gif"; //$NON-NLS-1$
	public static final String ICON_OPTIONS = "options.gif"; //$NON-NLS-1$
	public static final String ICON_REFRESH = "refresh.gif"; //$NON-NLS-1$
	public static final String ICON_DISCONNECT = "disconnect.gif"; //$NON-NLS-1$
	public static final String ICON_CONNECT = "connect.gif"; //$NON-NLS-1$
	
	// The shared instance
	private static LinuxToolsPlugin plugin;
	
	private static final String[] allIcons = {ICON_RUN, 
												ICON_PAUSE,
												ICON_OPTIONS,
												ICON_REFRESH,
												ICON_DISCONNECT,
												ICON_CONNECT}; 
	
	/**
	 * Returns the shared instance
	 * @return the shared instance
	 */
	public static LinuxToolsPlugin getDefault() {
		return plugin;
	}

	/**
	 * The constructor
	 */
	public LinuxToolsPlugin() {
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

	@Override
	protected void initializeImageRegistry() {
		String path = getIconPath();
		for (String s: allIcons) {
			putImageInRegistry(s, path + s);
		}
	}
}
