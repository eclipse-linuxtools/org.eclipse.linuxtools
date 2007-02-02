/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/

package org.eclipse.cdt.oprofile.ui;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.cdt.oprofile.core.Oprofile;
import org.eclipse.cdt.oprofile.ui.sample.SampleView;
import org.eclipse.cdt.oprofile.ui.system.SystemProfileView;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;


/**
 * The main plugin class to be used in the desktop.
 * @author keiths
 */
public class OprofilePlugin extends AbstractUIPlugin {
	//The shared instance.
	private static OprofilePlugin plugin;
	//Resource bundle.
	private ResourceBundle resourceBundle;

	private SampleView _sampleView = null;
	private SystemProfileView _sysProfileView = null;
	
	public static final String ID_PLUGIN = "org.eclipse.cdt.oprofile.ui"; //$NON-NLS-1$

	// Project Profiling View
	public static final String ID_VIEW_PROJECT = ID_PLUGIN + ".project"; //$NON-NLS-1$
	public static final String ID_VIEW_PROJECT_PROFILE = ID_VIEW_PROJECT + ".ProjectProfileView"; //$NON-NLS-1$

	// System Profiling View
	public static final String ID_VIEW_SYSTEM = ID_PLUGIN + ".system"; //$NON-NLS-1$
	public static final String ID_VIEW_SYSTEM_PROFILE = ID_VIEW_SYSTEM + ".SystemProfileView"; //$NON-NLS-1$

	// Sample View
	public static final String ID_VIEW_SAMPLE = ID_PLUGIN + ".sample"; //$NON-NLS-1$
	
	// Daemon View
	public static final String ID_VIEW_DAEMON = ID_PLUGIN + ".daemon"; //$NON-NLS-1$
	
	/**
	 * The constructor.
	 */
	public OprofilePlugin() {
		plugin = this;
		try {
			resourceBundle= ResourceBundle.getBundle("org.eclipse.cdt.oprofile.ui.OprofilePluginResources"); //$NON-NLS-1$
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}

		/* Make sure the kernel module is loaded (just in case
		   the user has not authenticated or the module couldn't
		   be loaded). */
		if (!Oprofile.isKernelModuleLoaded()) {
			Oprofile.initializeOprofileModule();
		}
	}

	/**
	 * Returns the shared instance.
	 */
	public static OprofilePlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the workspace instance.
	 */
	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle= OprofilePlugin.getDefault().getResourceBundle();
		try {
			return bundle.getString(key);
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}
	
	/**
	 * Returns the SampleView open onl the desktop or null if none
	 * @return the SampleView
	 */
	public SampleView getSampleView()
	{
		return _sampleView;
	}
	
	/**
	 * Registers the SampleView to use for viewing all IProfileElements
	 * @param view	the view to use (or null for none)
	 */
	public void setSampleView(SampleView view)
	{
		_sampleView = view;
	}
	
	public SystemProfileView getSystemProfileView() {
		return _sysProfileView;
	}
	
	public void setSystemProfileView(SystemProfileView view) {
		_sysProfileView = view;
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
