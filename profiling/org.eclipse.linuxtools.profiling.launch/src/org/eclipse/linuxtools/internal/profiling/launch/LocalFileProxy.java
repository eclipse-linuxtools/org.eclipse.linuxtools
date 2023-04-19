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
package org.eclipse.linuxtools.internal.profiling.launch;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.profiling.launch.IRemoteFileProxy;

public class LocalFileProxy implements IRemoteFileProxy {

    URI workingDirURI;

    public LocalFileProxy(URI uri) {
        workingDirURI=uri;
    }

    @Override
    public URI toURI(IPath path) {
        return path.toFile().toURI();
    }

    @Override
    public URI toURI(String path) {
        try {
            return new URI(path);
        } catch (URISyntaxException e) {
            return null;
        }
    }

    @Override
    public String toPath(URI uri) {
        return uri.getPath();
    }

    @Override
    public String getDirectorySeparator() {
        return File.separator;
    }

    @Override
    public IFileStore getResource(String path) {
        return EFS.getLocalFileSystem().getStore(new Path(path));
    }

    @Override
    public URI getWorkingDir() {
        return workingDirURI;
    }

}
