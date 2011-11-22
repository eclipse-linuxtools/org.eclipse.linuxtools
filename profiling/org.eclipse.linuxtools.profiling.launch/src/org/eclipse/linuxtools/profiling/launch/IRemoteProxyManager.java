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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

public interface IRemoteProxyManager {
	String EXTENSION_POINT_ID = "RemoteProxyManager"; //$NON-NLS-1$
	String MANAGER_NAME = "manager"; //$NON-NLS-1$
	String SCHEME_ID = "scheme"; //$NON-NLS-1$ 
	public IRemoteFileProxy getFileProxy(IProject project) throws CoreException;
	public IRemoteFileProxy getFileProxy(URI uri) throws CoreException;
	public IRemoteCommandLauncher getLauncher(IProject project) throws CoreException;
	public IRemoteCommandLauncher getLauncher(URI uri) throws CoreException;
	public String getOS(IProject project) throws CoreException;
	public String getOS(URI uri) throws CoreException;
}
