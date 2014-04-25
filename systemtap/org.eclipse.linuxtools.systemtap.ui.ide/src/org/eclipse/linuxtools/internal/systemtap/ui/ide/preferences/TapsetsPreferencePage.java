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

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.PathEditor;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.IDEPlugin;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.Localization;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;


public class TapsetsPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
    public TapsetsPreferencePage() {
        super(GRID);
        setPreferenceStore(IDEPlugin.getDefault().getPreferenceStore());
        setDescription(Localization.getString("TapsetsPreferencePage.AdditionalTapsetsCategory")); //$NON-NLS-1$
    }

    /**
     * Creates the field editors. Field editors are abstractions of
     * the common GUI blocks needed to manipulate various types
     * of preferences. Each field editor knows how to save and
     * restore itself.
     */
    @Override
    public void createFieldEditors() {
        addField(new PathEditor(IDEPreferenceConstants.P_TAPSETS,
                Localization.getString("TapsetsPreferencePage.AdditionalTapsets"), Localization.getString("TapsetsPreferencePage.TapsetDirectory"), getFieldEditorParent())); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public void init(IWorkbench workbench) {
    }
}
