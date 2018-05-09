/*******************************************************************************
 * Copyright (c) 2011, 2018 Red Hat Inc. and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
