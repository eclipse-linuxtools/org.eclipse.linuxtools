/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/

package org.eclipse.cdt.oprofile.core;

import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*;
import java.util.*;

/**
 * The main plugin class to be used in the desktop.
 * @author keiths
 */
public class OprofileCorePlugin extends Plugin {
	private static final String PLUGIN_ID = "org.eclipse.cdt.oprofile.core";

	//The shared instance.
	private static OprofileCorePlugin plugin;
	//Resource bundle.
	private ResourceBundle resourceBundle;
	
	private IOpxmlProvider _opxml = null;
	private IOpcontrolProvider _opcontrol = null;
	
	/**
	 * The constructor.
	 */
	public OprofileCorePlugin() {
		plugin = this;
		try {
			resourceBundle= ResourceBundle.getBundle("org.eclipse.cdt.oprofile.core.OprofileCorePluginResources"); //$NON-NLS-1$
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
	}

	/**
	 * Returns the shared instance.
	 */
	public static OprofileCorePlugin getDefault() {
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
		ResourceBundle bundle= OprofileCorePlugin.getDefault().getResourceBundle();
		if (bundle != null) {
			try {
				return bundle.getString(key);
			} catch (MissingResourceException e) {
			}
		}
		
		return key;
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}
	
	/**
	 * Returns the unique id of this plugin. Should match plugin.xml!
	 */
	public static String getId() {
		return PLUGIN_ID;
	}
	
	/**
	 * Returns the OpxmlProvider registered with the plugin or throws an exception
	 * @return the OpxmlProvider
	 * @throws OpxmlException
	 */
	public IOpxmlProvider getOpxmlProvider() throws OpxmlException {
		Exception except = null;
		
		if (_opxml == null) {
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IExtensionPoint extension = registry.getExtensionPoint(PLUGIN_ID, "OpxmlProvider"); //$NON-NLS-1$
			if (extension != null) {
				IExtension[] extensions = extension.getExtensions();
				IConfigurationElement[] configElements = extensions[0].getConfigurationElements();
				if (configElements.length != 0) {
					try {
						_opxml = (IOpxmlProvider) configElements[0].createExecutableExtension("class"); //$NON-NLS-1$
					} catch (CoreException ce) {
						except = ce;
					}
				}
			}
		}
		
		// If no provider found, throw a new exception
		if (_opxml == null) {
			String msg = getResourceString("opxmlProvider.error.missing"); //$NON-NLS-1$
			Status status = new Status(IStatus.ERROR, getId(), IStatus.OK, msg, except);
			throw new OpxmlException(status);
		}
		
		return _opxml;
	}
	
	/**
	 * Returns the registered opcontrol provider or throws an exception
	 * @return the OpcontrolProvider registered with the plugin
	 * @throws OpcontrolException
	 */
	public IOpcontrolProvider getOpcontrolProvider() throws OpcontrolException {
		Exception except = null;
		
		if (_opcontrol == null) {
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IExtensionPoint extension = registry.getExtensionPoint(PLUGIN_ID, "OpcontrolProvider"); //$NON-NLS-1$
			if (extension != null) {
				IExtension[] extensions = extension.getExtensions();
				IConfigurationElement[] configElements = extensions[0].getConfigurationElements();
				if (configElements.length != 0) {
					try {
						_opcontrol = (IOpcontrolProvider) configElements[0].createExecutableExtension("class"); //$NON-NLS-1$
					} catch (CoreException ce) {
						except = ce;
					}
				}
			}
		}
		
		// If there was a problem finding opcontrol, throw an exception
		if (_opcontrol == null) {
			String msg = getResourceString("opcontrolProvider.error.missing"); //$NON-NLS-1$
			Status status = new Status(IStatus.ERROR, getId(), IStatus.OK, msg, except);
			throw new OpcontrolException(status);
		}

		return _opcontrol;
	}
}
