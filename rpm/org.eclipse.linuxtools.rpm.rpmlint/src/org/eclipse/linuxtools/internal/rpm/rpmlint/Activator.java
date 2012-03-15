/*******************************************************************************
 * Copyright (c) 2007 Alphonse Van Assche.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alphonse Van Assche - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.rpm.rpmlint;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.linuxtools.internal.rpm.rpmlint.preferences.PreferenceConstants;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	/**
	 * The id of this plugin.
	 */
	public static final String PLUGIN_ID = "org.eclipse.linuxtools.rpm.rpmlint"; //$NON-NLS-1$
	
	/**
	 * Specfile extension constant i.e. .spec files.
	 */
	public static final String SPECFILE_EXTENSION = "spec"; //$NON-NLS-1$
	
	/**
	 * RPM extension constant, i.e. .rpm files.
	 */
	public static final String RPMFILE_EXTENSION = "rpm"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
		plugin = this;
	}

	/**
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/**
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
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
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
	
	/**
	 * Returns the rpmlint path stored in the preferences.
	 * @return The path to the rpmlint executable.
	 */
	public static String getRpmlintPath() {
		return plugin.getPreferenceStore().getString(
				PreferenceConstants.P_RPMLINT_PATH);
	}
}
