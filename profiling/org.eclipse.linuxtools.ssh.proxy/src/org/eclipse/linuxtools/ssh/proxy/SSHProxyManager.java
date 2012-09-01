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

import java.io.InputStream;
import java.io.IOException;
import java.net.URI;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.internal.ssh.proxy.SSHCommandLauncher;
import org.eclipse.linuxtools.internal.ssh.proxy.SSHFileProxy;
import org.eclipse.linuxtools.profiling.launch.IRemoteCommandLauncher;
import org.eclipse.linuxtools.profiling.launch.IRemoteFileProxy;
import org.eclipse.linuxtools.profiling.launch.IRemoteProxyManager;

public class SSHProxyManager implements IRemoteProxyManager {

	@Override
	public IRemoteFileProxy getFileProxy(URI uri) {
		return new SSHFileProxy(uri);
	}

	@Override
	public IRemoteFileProxy getFileProxy(IProject project) {
		return new SSHFileProxy(project.getLocationURI());
	}

	@Override
	public IRemoteCommandLauncher getLauncher(URI uri) {
		return new SSHCommandLauncher(uri);
	}

	@Override
	public IRemoteCommandLauncher getLauncher(IProject project) {
		return new SSHCommandLauncher(project.getLocationURI());
	}

	@Override
	public String getOS(URI uri) throws CoreException {
		SSHCommandLauncher cmdLauncher = new SSHCommandLauncher(uri);
		Process p = cmdLauncher.execute(new Path("/bin/uname"), new String[] {"-s"}, new String[0], null, null); //$NON-NLS-1$ //$NON-NLS-2$
		String os = ""; //$NON-NLS-1$
		try {
			InputStream in = p.getInputStream();
			int exit = p.waitFor();
			if (exit == 0) {
				byte bytes[] = new byte[15];
				int len;
				while ((len = in.read(bytes)) != -1)
					os = os + new String(bytes, 0, len);
				os = os.substring(0, os.indexOf("\n")); //$NON-NLS-1$
			}
		} catch (InterruptedException e) {
		} catch (IOException e2) {
		}
		return os;
	}

	@Override
	public String getOS(IProject project) throws CoreException {
		URI uri = project.getLocationURI();
		return getOS(uri);
	}
}
