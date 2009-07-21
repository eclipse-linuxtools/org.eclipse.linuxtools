/*******************************************************************************
 * Copyright (c) 2004-2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.core;

import java.io.File;
import java.io.IOException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * The main plugin class to be used in the desktop.
 */
public class RPMCorePlugin extends AbstractUIPlugin {
	//The shared instance.
	private static RPMCorePlugin plugin;
	//Resource bundle.
	private ResourceBundle resourceBundle;
	//Shell script shared by all external operations
	private File shellScriptFile;
	//Log file shared by all external operations
	private File externalLogFile;
	
	public static final String ID = "org.eclipse.linuxtools.rpm.core"; //$NON-NLS-1$
	
	
	/**
	 * The constructor.
	 */
	public RPMCorePlugin() {
		//super();
		plugin = this;
		try {
			resourceBundle= ResourceBundle.getBundle("org.eclipse.linuxtools.rpm.core.RPMPluginResources"); //$NON-NLS-1$
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
	}

	/**
	 * Returns the shared instance.
	 */
	public static RPMCorePlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle= RPMCorePlugin.getDefault().getResourceBundle();
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
	
	@Override
	protected void initializeDefaultPreferences(IPreferenceStore store)
		 {
		  String user_name = System.getProperty("user.name"); //$NON-NLS-1$
		  store.setDefault(IRPMConstants.RPM_DISPLAYED_LOG_NAME, ".logfilename_" //$NON-NLS-1$
		  		+ user_name);
		  store.setDefault(IRPMConstants.RPM_LOG_NAME, "rpmbuild.log"); //$NON-NLS-1$
	
		  store.setDefault(IRPMConstants.RPM_CMD, "/bin/rpm"); //$NON-NLS-1$
		  store.setDefault(IRPMConstants.RPMBUILD_CMD, "/usr/bin/rpmbuild"); //$NON-NLS-1$
		  store.setDefault(IRPMConstants.DIFF_CMD, "/usr/bin/diff"); //$NON-NLS-1$
		 }

	
	//Note this method is not thread-safe
	public File getShellScriptFile() throws CoreException {
		if(shellScriptFile == null) {
			try {
				shellScriptFile = File.createTempFile(RPMCorePlugin.ID, ".sh"); //$NON-NLS-1$
			} catch(IOException e) {
				String throw_message = Messages.getString("RPMCore.Error_creating__1") + //$NON-NLS-1$
				  shellScriptFile.getAbsolutePath();
				IStatus error = new Status(IStatus.ERROR, IRPMConstants.ERROR, 1,
						throw_message, null);
				throw new CoreException(error);
			}
		}
		return shellScriptFile;
	}
	
	//Note this method is not thread-safe
	public File getExternalLogFile() throws CoreException {
		if(externalLogFile == null) {
			try {
				externalLogFile = File.createTempFile(RPMCorePlugin.ID, ".log"); //$NON-NLS-1$
			} catch(IOException e) {
				String throw_message = Messages.getString("RPMCore.Error_creating__1") + //$NON-NLS-1$
				  externalLogFile.getAbsolutePath();
				IStatus error = new Status(IStatus.ERROR, IRPMConstants.ERROR, 1,
						throw_message, null);
				throw new CoreException(error);
			}
		}
		return externalLogFile;
	}
}
