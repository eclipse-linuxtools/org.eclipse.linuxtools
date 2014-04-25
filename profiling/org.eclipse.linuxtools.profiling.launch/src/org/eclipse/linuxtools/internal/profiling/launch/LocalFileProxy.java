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
package org.eclipse.linuxtools.internal.profiling.launch;

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
        return System.getProperty("file.separator"); //$NON-NLS-1$
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
