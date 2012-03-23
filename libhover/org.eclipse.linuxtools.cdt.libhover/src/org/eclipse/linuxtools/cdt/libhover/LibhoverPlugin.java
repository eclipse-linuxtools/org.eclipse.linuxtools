/*******************************************************************************
 * Copyright (c) 2009, 2011 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.cdt.libhover;

import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.linuxtools.internal.cdt.libhover.LibHover;
import org.eclipse.linuxtools.internal.cdt.libhover.LibHoverLoadJob;
import org.eclipse.linuxtools.internal.cdt.libhover.LibHoverMessages;
import org.eclipse.linuxtools.internal.cdt.libhover.preferences.PreferenceConstants;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class LibhoverPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.linuxtools.cdt.libhover";

	private static final String LOAD_JOB_TXT = "LibHover.LoadJob.txt"; //$NON-NLS-1$
	
	// The shared instance
	private static LibhoverPlugin plugin;
	
	static {
		plugin = new LibhoverPlugin();
	}

	/**
	 * The constructor
	 */
	public LibhoverPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		ResourcesPlugin.getWorkspace().addSaveParticipant(PLUGIN_ID,
				new ISaveParticipant() {
					public void saving(ISaveContext saveContext) throws CoreException {
						LibhoverPlugin.getDefault().save(saveContext);
					}				
					public void rollback(ISaveContext saveContext) {}
					public void prepareToSave(ISaveContext saveContext) throws CoreException {}
					public void doneSaving(ISaveContext saveContext) {}
				});
		IPreferenceStore ps = getPreferenceStore();
		if (ps == null || !ps.getBoolean(PreferenceConstants.LAZY_LOAD)) {
			Job k = new LibHoverLoadJob(LibHoverMessages.getString(LOAD_JOB_TXT));
			k.schedule();
		}
	}

	private void save(ISaveContext context) {
		LibHover.saveLibraries();
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
	public static LibhoverPlugin getDefault() {
		return plugin;
	}
	
	/*
	 * Returns the id of the plugin
	 */
	public static String getID() {
		return PLUGIN_ID;
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
}
