/*******************************************************************************
 * Copyright (c) 2006, 2015 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kyu Lee <klee@redhat.com> - initial API and implementation
 *    Jeff Johnston <jjohnstn@redhat.com> - add removed files support
 *******************************************************************************/
package org.eclipse.linuxtools.internal.changelog.core.actions;


import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IPath;

public class PatchFile {

    private static class EmptyStorage implements IStorage {

        public EmptyStorage() {
        }

        @Override
        public InputStream getContents() {
            return new ByteArrayInputStream(new byte[0]);
        }

        @Override
        public IPath getFullPath() {
            return null;
        }

        @Override
        public <T> T getAdapter(Class<T> adapter) {
            return null;
        }

        @Override
        public String getName() {
            return "__emptyStorage__";
        }

        @Override
        public boolean isReadOnly() {
            return true;
        }
    }

    private IStorage storage = new EmptyStorage();
    private ArrayList<PatchRangeElement> pranges = new ArrayList<>();

    private boolean newfile = false;
    private boolean removedfile = false;
    private IResource resource; // required only if dealing with change


    public boolean isNewfile() {
        return newfile;
    }

    public void setNewfile(boolean newfile) {
        this.newfile = newfile;
    }

    public boolean isRemovedFile() {
        return removedfile;
    }

    public void setRemovedFile(boolean removedfile) {
        this.removedfile = removedfile;
    }

    public PatchFile(IResource resource) {
        this.resource = resource;
    }

    public void addLineRange(int from, int to, boolean localChange) {

        pranges.add(new PatchRangeElement(from, to, localChange));
    }

    public PatchRangeElement[] getRanges() {
        Object[] tmpEle = pranges.toArray();
        PatchRangeElement[] ret = new PatchRangeElement[tmpEle.length];

        for (int i = 0; i < tmpEle.length; i++) {
            ret[i] = (PatchRangeElement) tmpEle[i];
        }
        return ret;
    }


    public IPath getPath() {
        return resource.getFullPath();
    }

    public IStorage getStorage() {
        return storage;
    }

    public void setStorage(IStorage storage) {
        this.storage = storage;
    }

    public IResource getResource() {
        return resource;
    }

    @Override
    public boolean equals(Object o) {

        if (!(o instanceof PatchFile))
            return false;

        PatchFile that = (PatchFile) o;
        // check  fpath  +  count
        if (!this.resource.equals(that.resource) ||
                this.pranges.size() != that.pranges.size() ) {
            return false;
        }

        // check range elements
        PatchRangeElement[] thatsrange = that.getRanges();

        for(int i=0; i<this.pranges.size();i++) {
            if (!thatsrange[i].equals(pranges.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = resource.hashCode();
        for(int i=0; i<this.pranges.size();i++) {
            hash += pranges.get(i).hashCode();
        }
        return hash;
    }
}