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
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.IDEPlugin;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.Localization;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;


public class EditorPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
    public EditorPreferencePage() {
        super(GRID);
        setPreferenceStore(IDEPlugin.getDefault().getPreferenceStore());
        setDescription(Localization.getString("EditorPreferencePage.EditorPreferenceDescription")); //$NON-NLS-1$
    }

    @Override
    public void createFieldEditors() {
        addField(new ColorFieldEditor(
                IDEPreferenceConstants.P_EDITOR_BACKGROUND,
                Localization.getString("EditorPreferencePage.BackgroundColor"), getFieldEditorParent())); //$NON-NLS-1$

        addField(new BooleanFieldEditor(
                IDEPreferenceConstants.P_SHOW_LINE_NUMBERS,
                Localization.getString("EditorPreferencePage.ShowLineNumbers"), //$NON-NLS-1$
                getFieldEditorParent()));
    }

    @Override
    public void init(IWorkbench workbench) {
    }
}
