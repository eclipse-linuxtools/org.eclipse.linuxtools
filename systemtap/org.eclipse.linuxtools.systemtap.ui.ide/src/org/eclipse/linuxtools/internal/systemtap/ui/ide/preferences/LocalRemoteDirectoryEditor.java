/*******************************************************************************
 * Copyright (c) 2012, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide.preferences;

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class LocalRemoteDirectoryEditor extends DirectoryFieldEditor {
    private boolean remote, isEnabled = true;
    public LocalRemoteDirectoryEditor(String name, String labelText, Composite parent, boolean remote) {
        super(name, labelText, parent);
        this.setRemote(remote, parent);
    }

    public boolean getRemote() {
        return this.remote;
    }

    public void setRemote(boolean remote, Composite parent) {
        this.remote = remote;
        Button b = getChangeControl(parent);
        if (isEnabled) {
            b.setEnabled(!remote);
        }
    }

    @Override
    public String changePressed() {
        if (this.remote) {
            return ""; //$NON-NLS-1$
        }
        return super.changePressed();
    }

    @Override
    protected boolean doCheckState() {
        if (this.remote) {
            return true;
        }
        return super.doCheckState();
    }

    @Override
    public void setEnabled(boolean enabled, Composite parent) {
        super.setEnabled(enabled, parent);
        isEnabled = enabled;
        this.setRemote(this.remote, parent);
    }
}
