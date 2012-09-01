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

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.provider.FileSystem;

public class SSHFileSystem extends FileSystem {

	@Override
	public IFileStore getStore(URI uri) {
		SSHProxyManager proxy = new SSHProxyManager();
		return proxy.getFileProxy(uri).getResource(uri.getPath());
	}
}
