/*******************************************************************************
 * Copyright (c) 2011 Red Hat Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.profiling.launch;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.internal.profiling.launch.LocalFileProxy;
import org.eclipse.linuxtools.internal.profiling.launch.LocalLauncher;
import org.eclipse.linuxtools.internal.profiling.launch.Messages;
import org.eclipse.linuxtools.internal.profiling.launch.ProfileLaunchPlugin;

public class RemoteProxyManager implements IRemoteProxyManager {
	
	private static final String EXT_ATTR_CLASS = "class"; //$NON-NLS-1$
	/**
	 * @since 2.1
	 */
	protected static final String LOCALSCHEME = "file"; //$NON-NLS-1$

	private static RemoteProxyManager manager;
	private LocalFileProxy lfp;
	/**
	 * @since 2.1
	 */
	protected RemoteProxyNatureMapping mapping = new RemoteProxyNatureMapping();
	private Map<String, IRemoteProxyManager> remoteManagers = new HashMap<String, IRemoteProxyManager>();
	
	/**
	 * @since 2.1
	 */
	protected RemoteProxyManager() {
		// do nothing
	}
	
	public static RemoteProxyManager getInstance() {
		if (manager == null)
			manager = new RemoteProxyManager();
		return manager;
	}
	
	LocalFileProxy getLocalFileProxy(URI uri) {
		if (lfp == null)
			lfp = new LocalFileProxy(uri);
		return lfp;
	}
	/**
	 * @since 2.1
	 */
	protected IRemoteProxyManager getRemoteManager(String schemeId) throws CoreException {
		IRemoteProxyManager remoteManager = remoteManagers.get(schemeId);
		if (remoteManager == null) {
			IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(ProfileLaunchPlugin.PLUGIN_ID, IRemoteProxyManager.EXTENSION_POINT_ID);
			IConfigurationElement[] infos = extensionPoint.getConfigurationElements();
			for(int i = 0; i < infos.length; i++) {
				IConfigurationElement configurationElement = infos[i];
				if (configurationElement.getName().equals(IRemoteProxyManager.MANAGER_NAME)) {
					if (configurationElement.getAttribute(IRemoteProxyManager.SCHEME_ID).equals(schemeId)) {
						Object obj = configurationElement.createExecutableExtension(EXT_ATTR_CLASS);
						if (obj instanceof IRemoteProxyManager) {
							remoteManager = (IRemoteProxyManager)obj;
							remoteManagers.put(schemeId, remoteManager);
							break;
						}
					}
				}
			}
		}
		return remoteManager;
	}

	public IRemoteFileProxy getFileProxy(URI uri) throws CoreException {
		String scheme = uri.getScheme();
		if (scheme != null && !scheme.equals(LOCALSCHEME)){
			IRemoteProxyManager manager = getRemoteManager(scheme);
			if (manager != null)
				return manager.getFileProxy(uri);
			else
				throw new CoreException(new Status(IStatus.ERROR, ProfileLaunchPlugin.PLUGIN_ID,
							IStatus.OK, Messages.RemoteProxyManager_unrecognized_scheme + scheme, null));
		}
		return getLocalFileProxy(uri);
	}

	public IRemoteFileProxy getFileProxy(IProject project) throws CoreException {
		if (project == null) {
			return getLocalFileProxy(null);
		}
		String scheme = mapping.getSchemeFromNature(project);
		if (scheme!=null) {
			IRemoteProxyManager manager = getRemoteManager(scheme);
			if (manager != null)
				return manager.getFileProxy(project);
		}
		URI projectURI = project.getLocationURI();
		return getFileProxy(projectURI);
	}

	public IRemoteCommandLauncher getLauncher(URI uri) throws CoreException {
		String scheme = uri.getScheme();
		if (scheme != null && !scheme.equals(LOCALSCHEME)){
			IRemoteProxyManager manager = getRemoteManager(scheme);
			if (manager != null)
		       return manager.getLauncher(uri);
		}
		return new LocalLauncher();
	}

	public IRemoteCommandLauncher getLauncher(IProject project) throws CoreException {
		if (project == null){
			return new LocalLauncher();
		}
		String scheme = mapping.getSchemeFromNature(project);
		if (scheme!=null) {
			IRemoteProxyManager manager = getRemoteManager(scheme);
			return manager.getLauncher(project);
		}
		URI projectURI = project.getLocationURI();
		return getLauncher(projectURI);
	}

	public String getOS(URI uri) throws CoreException {
		String scheme = uri.getScheme();
		if (scheme != null && !scheme.equals(LOCALSCHEME)){
			IRemoteProxyManager manager = getRemoteManager(scheme);
			if (manager != null)
			  return manager.getOS(uri);
		}
		return Platform.getOS();
	}

	public String getOS(IProject project) throws CoreException {
		String scheme = mapping.getSchemeFromNature(project);
		if (scheme!=null) {
			IRemoteProxyManager manager = getRemoteManager(scheme);
			return manager.getOS(project);
		}
		URI projectURI = project.getLocationURI();
		return getOS(projectURI);
	}


}
