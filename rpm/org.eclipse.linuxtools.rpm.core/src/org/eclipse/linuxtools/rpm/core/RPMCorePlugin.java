/*
 * (c) 2004, 2005 Red Hat, Inc.
 *
 * This program is open source software licensed under the 
 * Eclipse Public License ver. 1
 */

package org.eclipse.linuxtools.rpm.core;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.linuxtools.rpm.core.internal.Messages;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

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
	
	public static final String ID = "org.eclipse.linuxtools.rpm.core";
	
	
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

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}
	
	/**
	 * This method is called when the plug-in is stopped
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
	}
	
	/**
	 * Returns the workspace instance.
	 */
	public static IWorkspace getWorkspace() {
		return RPMCorePlugin.getWorkspace();
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
		  String user_name = System.getProperty("user.name");
		  store.setDefault(IRPMConstants.RPM_DISPLAYED_LOG_NAME, ".logfilename_" //$NON-NLS-1$
		  		+ user_name);
		  store.setDefault(IRPMConstants.RPM_LOG_NAME, "rpmbuild.log"); //$NON-NLS-1$
		  store.setDefault(IRPMConstants.AUTHOR_NAME, user_name);
		  store.setDefault(IRPMConstants.AUTHOR_EMAIL, user_name + "@" + getHostName()); //$NON-NLS-1$
	
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

	/** 
	* Method getHostName gets the name of the host to use as part of the
	* e-mail address for the changelog entry in the spec file.
	* @return String containing the name of the host, "" if error
	*/
   public static String getHostName()
	{
	   String hostname;
		 try {
			 hostname = java.net.InetAddress.getLocalHost().getHostName();
		 } catch (UnknownHostException e) {
			 return "";
		 }
		 // Trim off superflous stuff from the hostname
		 int firstdot = hostname.indexOf("."); //$NON-NLS-1$
		 int lastdot = hostname.lastIndexOf("."); //$NON-NLS-1$
		 // If the two are equal, no need to trim name
		 if (firstdot == lastdot) {
		   return hostname;
		 }
		 String hosttemp = ""; //$NON-NLS-1$
		 String hosttemp2 = hostname;
		 while (firstdot != lastdot) {
		   hosttemp = hosttemp2.substring(lastdot) + hosttemp;
		   hosttemp2 = hostname.substring(0,lastdot);
		   lastdot = hosttemp2.lastIndexOf("."); //$NON-NLS-1$
		 }
		 return hosttemp.substring(1);
	}
   
}
