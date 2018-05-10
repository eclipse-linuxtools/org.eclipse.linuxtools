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

public class SourceEditorInput extends StorageEditorInput {

    /**
     * @param storage that represents a source file
     */
    public SourceEditorInput(IStorage storage) {
        super(storage);
    }

    @Override
    public boolean exists() {
        return false;
    }

}
