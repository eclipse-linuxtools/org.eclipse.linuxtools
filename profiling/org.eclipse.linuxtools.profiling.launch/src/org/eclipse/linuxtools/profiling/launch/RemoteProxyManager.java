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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.linuxtools.internal.profiling.launch.LocalFileProxy;
import org.eclipse.linuxtools.internal.profiling.launch.LocalLauncher;
import org.eclipse.linuxtools.internal.profiling.launch.ProfileLaunchPlugin;

public class RemoteProxyManager implements IRemoteProxyManager {
	
	public final static String RDT_NATURE = "org.eclipse.ptp.rdt.core.remoteNature"; //$NON-NLS-1$

	private static final String EXT_ATTR_CLASS = "class"; //$NON-NLS-1$
	
	private static RemoteProxyManager manager;
	private LocalFileProxy lfp;
	private Map<String, IRemoteProxyManager> remoteManagers = new HashMap<String, IRemoteProxyManager>();
	
	private RemoteProxyManager() {
		// do nothing
	}
	
	public static RemoteProxyManager getInstance() {
		if (manager == null)
			manager = new RemoteProxyManager();
		return manager;
	}
	
	LocalFileProxy getLocalFileProxy() {
		if (lfp == null)
			lfp = new LocalFileProxy();
		return lfp;
	}
	
	private IRemoteProxyManager getRemoteManager(String natureID) throws CoreException {
		IRemoteProxyManager remoteManager = remoteManagers.get(natureID);
		if (remoteManager == null) {
			IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(ProfileLaunchPlugin.PLUGIN_ID, IRemoteProxyManager.EXTENSION_POINT_ID);
			IConfigurationElement[] infos = extensionPoint.getConfigurationElements();
			for(int i = 0; i < infos.length; i++) {
				IConfigurationElement configurationElement = infos[i];
				if (configurationElement.getName().equals(IRemoteProxyManager.MANAGER_NAME)) {
					if (configurationElement.getAttribute(IRemoteProxyManager.NATURE_ID).equals(natureID)) {
						Object obj = configurationElement.createExecutableExtension(EXT_ATTR_CLASS);
						if (obj instanceof IRemoteProxyManager) {
							remoteManager = (IRemoteProxyManager)obj;
							remoteManagers.put(natureID, remoteManager);
							break;
						}
					}
				}
			}
		}
		return remoteManager;
	}
	
	public IRemoteFileProxy getFileProxy(IProject project) throws CoreException {
		if (project.hasNature(RDT_NATURE))
			return getRemoteManager(RDT_NATURE).getFileProxy(project);
		return getLocalFileProxy();
	}
	
	public IRemoteCommandLauncher getLauncher(IProject project) throws CoreException {
		return new LocalLauncher();
	}

	public String getOS(IProject project) throws CoreException {
		return Platform.getOS();
	}
}
