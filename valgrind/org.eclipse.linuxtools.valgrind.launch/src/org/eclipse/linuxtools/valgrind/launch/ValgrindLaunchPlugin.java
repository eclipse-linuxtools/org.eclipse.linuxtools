/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.linuxtools.valgrind.launch;

import java.io.File;
import java.util.HashMap;
import java.util.Set;

import org.eclipse.cdt.launch.LaunchUtils;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class ValgrindLaunchPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.linuxtools.valgrind.launch"; //$NON-NLS-1$
	public static final String LAUNCH_ID = PLUGIN_ID + ".valgrindLaunch"; //$NON-NLS-1$
	
	// Extension point constants
	public static final String TOOL_EXT_ID = "valgrindTools"; //$NON-NLS-1$
	public static final String TOOL_EXT_DEFAULT = PLUGIN_ID + ".memcheck"; //$NON-NLS-1$
	
	protected static final String EXT_ELEMENT = "tool"; //$NON-NLS-1$
	protected static final String EXT_ATTR_NAME = "name"; //$NON-NLS-1$
	protected static final String EXT_ATTR_ID = "id"; //$NON-NLS-1$
	protected static final String EXT_ATTR_PAGE = "page"; //$NON-NLS-1$
	protected static final String EXT_ATTR_DELEGATE = "delegate"; //$NON-NLS-1$
	
	
	// LaunchConfiguration constants
	public static final String ATTR_TOOL = PLUGIN_ID + ".TOOL"; //$NON-NLS-1$
	
	public static final String ATTR_GENERAL_TRACECHILD = PLUGIN_ID + ".GENERAL_TRACECHILD"; //$NON-NLS-1$
	public static final String ATTR_GENERAL_CHILDSILENT = PLUGIN_ID + ".GENERAL_CHILDSILENT"; //$NON-NLS-1$
//	public static final String ATTR_GENERAL_TRACKFDS = PLUGIN_ID + ".GENERAL_TRACKFDS";
//	public static final String ATTR_GENERAL_TIMESTAMP = PLUGIN_ID + ".GENERAL_TIMESTAMP";
	public static final String ATTR_GENERAL_FREERES = PLUGIN_ID + ".GENERAL_FREERES"; //$NON-NLS-1$
	public static final String ATTR_GENERAL_DEMANGLE = PLUGIN_ID + ".GENERAL_DEMANGLE"; //$NON-NLS-1$
	public static final String ATTR_GENERAL_NUMCALLERS = PLUGIN_ID + ".GENERAL_NUMCALLERS"; //$NON-NLS-1$
	public static final String ATTR_GENERAL_ERRLIMIT = PLUGIN_ID + ".GENERAL_ERRLIMIT"; //$NON-NLS-1$
	public static final String ATTR_GENERAL_BELOWMAIN = PLUGIN_ID + ".GENERAL_BELOWMAIN"; //$NON-NLS-1$
	public static final String ATTR_GENERAL_MAXFRAME = PLUGIN_ID + ".GENERAL_MAXFRAME"; //$NON-NLS-1$
	public static final String ATTR_GENERAL_SUPPFILE = PLUGIN_ID + ".GENERAL_SUPPFILE"; //$NON-NLS-1$
	
	protected HashMap<String, IConfigurationElement> toolMap; 
	
	// The shared instance
	private static ValgrindLaunchPlugin plugin;
	
	/**
	 * The constructor
	 */
	public ValgrindLaunchPlugin() {
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
	public static ValgrindLaunchPlugin getDefault() {
		return plugin;
	}
	
	public String[] getRegisteredToolIDs() {
		Set<String> ids = getToolMap().keySet();
		return ids.toArray(new String[ids.size()]);
	}
	
	public String getToolName(String id) {
		String name = null;
		IConfigurationElement config = getToolMap().get(id);
		if (config != null) {
			name = config.getAttribute(EXT_ATTR_NAME);
		}
		return name;
	}
	
	public IValgrindToolPage getToolPage(String id) throws CoreException {
		IValgrindToolPage tab = null;
		IConfigurationElement config = getToolMap().get(id);
		if (config != null) {
			Object obj = config.createExecutableExtension(EXT_ATTR_PAGE);
			if (obj instanceof IValgrindToolPage) {
				tab = (IValgrindToolPage) obj;
			}
		}
		if (tab == null) {
			throw new CoreException(new Status(IStatus.ERROR, PLUGIN_ID, Messages.getString("ValgrindLaunchPlugin.Cannot_retrieve_page"))); //$NON-NLS-1$
		}
		return tab;
	}
	
	public IValgrindLaunchDelegate getToolDelegate(String id) throws CoreException {
		IValgrindLaunchDelegate delegate = null;
		IConfigurationElement config = getToolMap().get(id);
		if (config != null) {
			Object obj = config.createExecutableExtension(EXT_ATTR_DELEGATE);
			if (obj instanceof IValgrindLaunchDelegate) {
				delegate = (IValgrindLaunchDelegate) obj;
			}
		}
		if (delegate == null) {
			throw new CoreException(new Status(IStatus.ERROR, PLUGIN_ID, Messages.getString("ValgrindLaunchPlugin.Cannot_retrieve_delegate"))); //$NON-NLS-1$
		}
		return delegate;
	}
	
	public File parseWSPath(String strpath) throws CoreException {
		strpath = LaunchUtils.getStringVariableManager().performStringSubstitution(strpath);
		IPath path = new Path(strpath);
		File suppfile = null;
		if (path.isAbsolute()) {
			suppfile = new File(path.toOSString());
		}
		else {
			IResource res = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
			if (res != null) {
				IPath absPath = res.getLocation();
				if (absPath != null) {
					suppfile = absPath.toFile();
				}
			}
		}		
		return suppfile;
	}
	
	protected void initializeToolMap() {
		toolMap = new HashMap<String, IConfigurationElement>();
		IExtensionPoint extPoint = Platform.getExtensionRegistry().getExtensionPoint(PLUGIN_ID, TOOL_EXT_ID);
		IConfigurationElement[] configs = extPoint.getConfigurationElements();
		for (IConfigurationElement config : configs) {
			if (config.getName().equals(EXT_ELEMENT)) {
				String id = config.getAttribute(EXT_ATTR_ID);
				if (id != null && config.getAttribute(EXT_ATTR_NAME) != null
						&& config.getAttribute(EXT_ATTR_PAGE) != null
						&& config.getAttribute(EXT_ATTR_DELEGATE) != null) {
					toolMap.put(id, config);
				}
			}
		}
	}

	protected HashMap<String, IConfigurationElement> getToolMap() {
		if (toolMap == null) {
			initializeToolMap();
		}
		return toolMap;
	}

	public String escapeAndQuote(String canonicalPath) {
		String ret = canonicalPath.replaceAll(" ", "\\ "); //$NON-NLS-1$ //$NON-NLS-2$
		return ret;
	}
}
