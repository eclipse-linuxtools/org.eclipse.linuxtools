/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.callgraph.launch;


import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.linuxtools.internal.callgraph.core.LaunchConfigurationConstants;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 * 
 * This is more or less the default Eclipse plugin-wizard Activator.
 */
public class CallgraphLaunchPlugin extends AbstractUIPlugin {

	// The shared instance
	private static CallgraphLaunchPlugin plugin;
	
	/**
	 * The constructor
	 */
	public CallgraphLaunchPlugin() {
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
	public static CallgraphLaunchPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(LaunchConfigurationConstants.PLUGIN_ID, path);
	}

	public static Shell getActiveWorkbenchShell() {
		return getDefault().getWorkbench().getActiveWorkbenchWindow().getShell();
	}
}


//public String getPluginLocation() {
//Bundle bundle = getBundle();
//
//URL locationUrl = FileLocator.find(bundle,new Path("/"), null);
//URL fileUrl = null;
//try {
//	fileUrl = FileLocator.toFileURL(locationUrl);
//} catch (IOException e) {
//	e.printStackTrace();
//}
//return fileUrl.getFile();
//}

//private void checkRun() {
////Check if install script has been executed. Install script MUST delete the FirstRun file,
////this will execute every time!
//String firstRun = getPluginLocation() + "FirstRun";
//File firstFile = new File(firstRun);
//if (firstFile.exists()) {
//	Shell sh = new Shell();
//	String command = "./" + getPluginLocation() + "install.sh " + firstRun;
//	InputDialog id = new InputDialog(sh, "First time startup", "Hi there! Looks like this is your first time running the SystemTap Eclipse plugin. In order for this plugin to work, you will first need to install SystemTap. Then please open a terminal and execute the following command. ", command, null);
//	id.open();
//}
//}
