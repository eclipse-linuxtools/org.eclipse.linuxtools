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

import org.eclipse.core.resources.IStorage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;

/**
 * An IStorageEditorInput fixture. Used in CParserTest.
 *
 */
public class CStringStorageInput implements IStorageEditorInput {

    private IStorage storage;

    public CStringStorageInput(IStorage storage) {
        this.storage = storage;
    }

    @Override
    public boolean exists() {
        return true;
    }

    @Override
    public ImageDescriptor getImageDescriptor() {
        return null;
    }

    @Override
    public String getName() {
        return storage.getName();
    }

    @Override
    public IPersistableElement getPersistable() {
        return null;
    }

    @Override
    public IStorage getStorage() {
        return storage;
    }

    @Override
    public String getToolTipText() {
        return "String-based file: " + storage.getName();
    }

    @Override
    public <T> T getAdapter(Class<T> adapter) {
        return null;
    }

}
