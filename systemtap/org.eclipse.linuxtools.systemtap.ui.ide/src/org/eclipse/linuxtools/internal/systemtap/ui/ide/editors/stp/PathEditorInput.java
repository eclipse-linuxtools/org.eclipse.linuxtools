/*******************************************************************************
 * Copyright (c) 2006, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.editors.text.ILocationProvider;
import org.eclipse.ui.ide.FileStoreEditorInput;

public class PathEditorInput extends FileStoreEditorInput implements IPathEditorInput, ILocationProvider {
    private IPath fPath;

    public PathEditorInput(IPath path) {
        super(EFS.getLocalFileSystem().getStore(path));
        if (path == null) {
            throw new IllegalArgumentException();
        }
        this.fPath = path;
    }

    @Override
    public int hashCode() {
        return fPath.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof PathEditorInput) {
            PathEditorInput other = (PathEditorInput) obj;
            return fPath.equals(other.fPath);
        } else if (obj instanceof FileStoreEditorInput) {
            return super.equals(obj);
        }

        return false;
    }

    @Override
    public String getName() {
        String[] substr = fPath.segments();
        return substr[substr.length -1];
    }

    @Override
    public IPath getPath() {
        return fPath;
    }

    @Override
    public IPath getPath(Object element) {
        if(element instanceof PathEditorInput) {
            return ((PathEditorInput)element).getPath();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
	@Override
    public <T> T getAdapter(Class<T> adapter) {
        if (PathEditorInput.class.equals(adapter)
                || IPathEditorInput.class.equals(adapter)
                || ILocationProvider.class.equals(adapter)) {
            return (T)this;
        }
        return Platform.getAdapterManager().getAdapter(this, adapter);
    }
}
