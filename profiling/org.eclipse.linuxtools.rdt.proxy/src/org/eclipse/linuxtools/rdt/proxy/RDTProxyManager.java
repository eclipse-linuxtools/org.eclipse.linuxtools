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
package org.eclipse.linuxtools.rdt.proxy;

import java.net.URI;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.internal.rdt.proxy.RDTCommandLauncher;
import org.eclipse.linuxtools.internal.rdt.proxy.RDTFileProxy;
import org.eclipse.linuxtools.profiling.launch.IRemoteCommandLauncher;
import org.eclipse.linuxtools.profiling.launch.IRemoteFileProxy;
import org.eclipse.linuxtools.profiling.launch.IRemoteProxyManager;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;

public class RDTProxyManager implements IRemoteProxyManager {

	@Override
	public IRemoteFileProxy getFileProxy(IProject project) throws CoreException {
		return new RDTFileProxy(project);
	}

	@Override
	public IRemoteCommandLauncher getLauncher(IProject project)
			throws CoreException {
		return new RDTCommandLauncher(project);
	}

	@Override
	public String getOS(IProject project) throws CoreException {
		// TODO Auto-generated method stub
		URI uri = project.getLocationURI();
		IRemoteServices services = PTPRemoteCorePlugin.getDefault().getRemoteServices(uri);
		IRemoteConnection connection = services.getConnectionManager().getConnection(uri);
		return connection.getProperty(IRemoteConnection.OS_NAME_PROPERTY);
	}

}
