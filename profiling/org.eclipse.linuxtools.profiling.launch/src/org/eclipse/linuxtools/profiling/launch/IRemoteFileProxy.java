/*******************************************************************************
 * Copyright (c) 2011, 2014 Red Hat Inc..
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

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IPath;

public interface IRemoteFileProxy {

	URI toURI(IPath path);
	URI toURI(String path);
	String toPath(URI uri);
	String getDirectorySeparator();
	IFileStore getResource(String path);
	/**
	 * @return Returns the working directory.
	 * @since 2.0
	 */
	URI getWorkingDir();

}
