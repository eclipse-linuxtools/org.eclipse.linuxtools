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

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.TapsetLibrary;
import org.eclipse.linuxtools.systemtap.ui.consolelog.internal.ConsoleLogPlugin;
import org.eclipse.linuxtools.systemtap.ui.consolelog.preferences.ConsoleLogPreferenceConstants;
import org.eclipse.linuxtools.systemtap.ui.consolelog.structures.ScriptConsole;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the IDE. This class contains lifecycle controls
 * for the plugin.
 * @see org.eclipse.ui.plugin.AbstractUIPlugin
 * @author Ryan Morse
 */
public class IDEPlugin extends AbstractUIPlugin {
	private IWorkbenchListener workbenchListener;
	private static IDEPlugin plugin;
	public static final String PLUGIN_ID = "org.eclipse.linuxtools.systemtap.ui.ide"; //$NON-NLS-1$

	public IDEPlugin() {
		plugin = this;
	}

	/**
	 * Called by the Eclipse Workbench at plugin activation time. Starts the plugin lifecycle.
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);

		workbenchListener = new IDECloseMonitor();
		plugin.getWorkbench().addWorkbenchListener(workbenchListener);
		TapsetLibrary.init();
	}

	/**
	 * Called by the Eclipse Workbench to deactivate the plugin.
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		super.stop(context);

		ScriptConsole.stopAll();

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

	/**
	 * Create an uri to be used to connect to the remote machine
	 */
	public URI createRemoteUri(String path) {
		IPreferenceStore p = ConsoleLogPlugin.getDefault().getPreferenceStore();
		String user = p.getString(ConsoleLogPreferenceConstants.SCP_USER);
		String host = p.getString(ConsoleLogPreferenceConstants.HOST_NAME);
		if (path == null)
		 {
			path = ""; //$NON-NLS-1$
		}
		try {
			URI uri = new URI("ssh", user, host, -1, path, null, null); //$NON-NLS-1$
			return uri;
		} catch (URISyntaxException uri) {
			return null;
		}
	}
}
