/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide;

import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.linuxtools.systemtap.ui.consolelog.actions.StopScriptAction;
import org.osgi.framework.BundleContext;


/**
 * The main plugin class to be used in the IDE. This class contains lifecycle controls
 * for the plugin.
 * @see org.eclipse.ui.plugin.AbstractUIPlugin
 * @author Ryan Morse
 */
public class IDEPlugin extends AbstractUIPlugin {
	public IDEPlugin() {
		plugin = this;
	}

	/**
	 * Called by the Eclipse Workbench at plugin activation time. Starts the plugin lifecycle.
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		
		workbenchListener = new IDECloseMonitor();
		plugin.getWorkbench().addWorkbenchListener(workbenchListener);
	}

	/**
	 * Called by the Eclipse Workbench to deactivate the plugin. 
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		
		StopScriptAction ssa = new StopScriptAction();
		ssa.init(PlatformUI.getWorkbench().getActiveWorkbenchWindow());
		ssa.stopAll();

		plugin.getWorkbench().removeWorkbenchListener(workbenchListener);
		
		plugin = null;
	}

	/**
	 * Returns this plugin's instance.
	 */
	public static IDEPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path.
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
	
	private IWorkbenchListener workbenchListener;
	private static IDEPlugin plugin;
	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.linuxtools.systemtap.ui.ide";

}
