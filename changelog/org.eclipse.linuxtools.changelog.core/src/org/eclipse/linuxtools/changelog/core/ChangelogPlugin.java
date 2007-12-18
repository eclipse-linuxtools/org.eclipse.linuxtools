/*******************************************************************************
 * Copyright (c) 2006 Phil Muldoon <pkmuldoon@picobot.org>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Phil Muldoon <pmuldoon@redhat.com> - initial API and implementation
 *    Kyu Lee <klee@redhat.com>          - editor support
 *******************************************************************************/
package org.eclipse.linuxtools.changelog.core;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IWorkspace;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * @author pmuldoon (Phil Muldoon)
 */

/**
 * The main plugin class to be used in the desktop.
 */
public class ChangelogPlugin extends AbstractUIPlugin {
	
	public final static String PLUGIN_ID = "org.eclipse.linuxtools.changelog.core"; // $NON-NLS-1$
	
	// The shared instance.
	private static ChangelogPlugin plugin;

	// Resource bundle.
	private ResourceBundle resourceBundle;

	/**
	 * The constructor.
	 */
	public ChangelogPlugin() {
		// super();
		plugin = this;
		try {
			resourceBundle = ResourceBundle
					.getBundle("org.eclipse.linuxtools.changelog.core.ChangelogPluginResources");
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}

	}

	/**
	 * Returns the shared instance.
	 */
	public static ChangelogPlugin getDefault() {
		return plugin;
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
	}

	/**
	 * Returns the workspace instance.
	 */
	public static IWorkspace getWorkspace() {
		return ChangelogPlugin.getWorkspace();
	}

	/**
	 * Returns the string from the plugin's resource bundle, or 'key' if not
	 * found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = ChangelogPlugin.getDefault()
				.getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
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

	protected void initializeDefaultPreferences(IPreferenceStore store) {
		store.setDefault("IChangeLogConstants.DATE_FORMAT", "true");
		store.setDefault("IChangeLogConstants.APPEND_RESOURCE_PATH", "false");

		store.setDefault("IChangeLogConstants.AUTHOR_NAME",
				ChangeLogPreferencesPage.getUserRealName());
		store.setDefault("IChangeLogConstants.AUTHOR_EMAIL",
				ChangeLogPreferencesPage.getUserName() + "@"
						+ ChangeLogPreferencesPage.getHostName());
		store.setDefault("IChangeLogConstants.DEFAULT_FORMATTER", "GNU Style");
		store.setDefault("IChangeLogConstants.DEFAULT_EDITOR", "GNU Editor");

	}
}
