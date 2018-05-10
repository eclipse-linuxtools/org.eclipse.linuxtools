/*******************************************************************************
 * Copyright (c) 2010, 2018 Red Hat Inc. and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.linuxtools.changelog.tests.fixtures;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IPath;

/**
 * Helper class for a IStorageEditorInput fixture.
 *
 */
public class CStringStorage implements IStorage {
    private String string;

    public CStringStorage(String input) {
        this.string = input;
    }

    @Override
    public InputStream getContents() {
        return new ByteArrayInputStream(string.getBytes());
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
        int len = Math.min(5, string.length());
        return string.substring(0, len).concat("..."); //$NON-NLS-1$
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }
}
