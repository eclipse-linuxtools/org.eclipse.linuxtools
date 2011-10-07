package org.eclipse.linuxtools.profiling.launch;

import org.eclipse.cdt.utils.Platform;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.internal.profiling.launch.LocalFileProxy;
import org.eclipse.linuxtools.internal.profiling.launch.LocalLauncher;

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
public class RemoteProxyManager implements IRemoteProxyManager {
	
	public final static String RDT_NATURE = "org.eclipse.ptp.rdt";
	
	private static RemoteProxyManager manager;
	private LocalFileProxy lfp;
	private IRemoteProxyManager remoteManager;
	
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
	
	private IRemoteProxyManager getRemoteManager() {
		if (remoteManager == null) {
		}
		return remoteManager;
	}
	
	public IRemoteFileProxy getFileProxy(IProject project) throws CoreException {
		if (project.hasNature(RDT_NATURE))
			return getRemoteManager().getFileProxy(project);
		return getLocalFileProxy();
	}
	
	public IRemoteCommandLauncher getLauncher(IProject project) throws CoreException {
		return new LocalLauncher();
	}

	public String getOS(IProject project) throws CoreException {
		return Platform.getOS();
	}
}
