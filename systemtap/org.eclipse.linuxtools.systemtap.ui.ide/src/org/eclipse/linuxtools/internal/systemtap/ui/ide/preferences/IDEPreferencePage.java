/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.IDEPlugin;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.Localization;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;


public class IDEPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
    public IDEPreferencePage() {
        super(GRID);
        setPreferenceStore(IDEPlugin.getDefault().getPreferenceStore());
        setDescription("Preferences when loading the IDE perspective"); //$NON-NLS-1$
    }

    @Override
    public void createFieldEditors() {
        addField(new BooleanFieldEditor(
                IDEPreferenceConstants.P_STORED_TREE,
                Localization.getString("IDEPreferencePage.UseStoredTapsetTree"), //$NON-NLS-1$
                getFieldEditorParent()));
        addField(new BooleanFieldEditor(
                IDEPreferenceConstants.P_REMOTE_PROBES,
                Localization.getString("IDEPreferencePage.RemoteProbes"), //$NON-NLS-1$
                getFieldEditorParent()));
    }

    @Override
    public void init(IWorkbench workbench) {
    }

}
