/*******************************************************************************
 * Copyright (c) 2011, 2012 Red Hat Inc and Others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *     IBM Corporation - Adapting to ssh
 *******************************************************************************/
package org.eclipse.linuxtools.ssh.proxy;

import java.net.URI;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.internal.ssh.proxy.SSHCommandLauncher;
import org.eclipse.linuxtools.internal.ssh.proxy.SSHFileProxy;
import org.eclipse.linuxtools.profiling.launch.IRemoteCommandLauncher;
import org.eclipse.linuxtools.profiling.launch.IRemoteFileProxy;
import org.eclipse.linuxtools.profiling.launch.IRemoteProxyManager;

public class SSHProxyManager implements IRemoteProxyManager {

	@Override
	public IRemoteFileProxy getFileProxy(URI uri) throws CoreException {
		return new SSHFileProxy(uri);
	}

	@Override
	public IRemoteFileProxy getFileProxy(IProject project) throws CoreException {
		return new SSHFileProxy(project.getLocationURI());
	}

	@Override
	public IRemoteCommandLauncher getLauncher(URI uri)
			throws CoreException {
		return new SSHCommandLauncher(uri);
	}

	@Override
	public IRemoteCommandLauncher getLauncher(IProject project)
			throws CoreException {
		return new SSHCommandLauncher(project.getLocationURI());
	}

	@Override
	public String getOS(URI uri) throws CoreException {
		//FIXME: need better way to get this property
		return "Linux"; //$NON-NLS-1$
	}

	@Override
	public String getOS(IProject project) throws CoreException {
		URI uri = project.getLocationURI();
		return getOS(uri);
	}
}
