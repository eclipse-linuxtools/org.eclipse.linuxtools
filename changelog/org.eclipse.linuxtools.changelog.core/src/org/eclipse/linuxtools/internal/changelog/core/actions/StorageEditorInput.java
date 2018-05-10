/*******************************************************************************
 * Copyright (c) 2007, 2018 Wind River Systems and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.changelog.core.actions;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;

/**
 * Abstract implementation of <code>IStorageEditorInput</code>.
 */
public abstract class StorageEditorInput implements IStorageEditorInput {

    /**
     * Storage associated with this editor input
     */
    private IStorage fStorage;

    /**
     * Constructs an editor input on the given storage
     */
    public StorageEditorInput(IStorage storage) {
        fStorage = storage;
    }

    @Override
    public IStorage getStorage() {
        return fStorage;
    }

    @Override
    public ImageDescriptor getImageDescriptor() {
        return null;
    }

    @Override
    public String getName() {
        return getStorage().getName();
    }

    @Override
    public IPersistableElement getPersistable() {
        return null;
    }

    @Override
    public String getToolTipText() {
        return getStorage().getFullPath().toOSString();
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        try {
            return object instanceof IStorageEditorInput
                && getStorage().equals(((IStorageEditorInput)object).getStorage());
        } catch (CoreException e) {
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getStorage().hashCode();
    }

    @Override
    public <T> T getAdapter(Class<T> adapter) {
        return null;
    }

}
