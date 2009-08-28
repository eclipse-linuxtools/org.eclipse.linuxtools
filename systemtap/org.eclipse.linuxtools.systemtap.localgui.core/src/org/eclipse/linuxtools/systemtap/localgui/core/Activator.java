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

package org.eclipse.linuxtools.systemtap.localgui.core;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;


/**
 * 	Activator class behaves like standard Wizard-created activator,
 *  except for the checkRun() function.
 *
 */
public class Activator extends AbstractUIPlugin {

	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		PluginConstants.setPluginLocation(getPluginLocation());
		PluginConstants.setWorkspaceLocation(getDefault().getStateLocation().toString()+"/"); //$NON-NLS-1$
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
	public static Activator getDefault() {
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
	
	public String getPluginLocation() {
		Bundle bundle = getBundle();

		URL locationUrl = FileLocator.find(bundle,new Path("/"), null); //$NON-NLS-1$
		URL fileUrl = null;
		try {
			fileUrl = FileLocator.toFileURL(locationUrl);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fileUrl.getFile();
		
	}

	
	
	/**
	 *  Check if install script has been executed. Install script MUST delete the FirstRun file,
	 *	or else this will execute every time!
	 *
	 *	Opens an install script to prompt user to properly install the plugin.
	 */
	//@SuppressWarnings("static-access")
/*	private void checkRun() {
		
		PluginConstants.setPluginLocation(getPluginLocation());
		File initFile = new File(PluginConstants.PLUGIN_LOCATION + "SystemTapPlugin.init");//$NON-NLS-1
		String line;

		try {
			BufferedReader br = new BufferedReader (new FileReader(initFile));
			while ( (line = br.readLine()) != null) {
				if (line.contains("First time")) {
				Shell sh = new Shell();
				//String command = "SystemTapPluginInstall/" + "install.sh"; //$NON-NLS-1$ $NON-NLS-2$
				MessageDialog.openInformation(sh, Messages.getString("Activator.0"), Messages.getString("Activator.1")); //$NON-NLS-1$ //$NON-NLS-2$
				BufferedWriter bw = new BufferedWriter(new FileWriter(initFile));
				bw.append("Easter egg");
				bw.close();
				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}*/


}
