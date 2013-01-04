/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.ssh.proxy;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IPath;
import org.eclipse.linuxtools.profiling.launch.IRemoteFileProxy;

public class SSHFileProxy extends SSHBase implements IRemoteFileProxy {

	URI uri;
	
	public SSHFileProxy(URI uri) {
		super(uri);
		this.uri = uri;
	}

	@Override
	public URI toURI(IPath path) {
		return uri;
	}

	@Override
	public URI toURI(String path) {
		return uri.resolve(path);
	}

	@Override
	public String toPath(URI uri) {
		return uri.getPath();
	}

	@Override
	public String getDirectorySeparator() {
		return "/"; //$NON-NLS-1$
	}

	@Override
	public IFileStore getResource(String path) {
		try {
			URI newUri = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(),
					path, uri.getQuery(), uri.getFragment());
			return new SSHFileStore(newUri, this);
		} catch (URISyntaxException e) {
			//This is not suppose to happen
			return null;
		}
	}

	@Override
	public URI getWorkingDir() {
		return uri;
	}
}
