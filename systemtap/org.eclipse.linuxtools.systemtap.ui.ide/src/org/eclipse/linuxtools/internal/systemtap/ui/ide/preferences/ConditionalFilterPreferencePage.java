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
import org.eclipse.linuxtools.internal.systemtap.ui.ide.IDEPlugin;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.Localization;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;



public class ConditionalFilterPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
    public ConditionalFilterPreferencePage() {
        super(GRID);
        setPreferenceStore(IDEPlugin.getDefault().getPreferenceStore());
        setDescription(Localization.getString("ConditionalFilterPreferencePage.ConditiionalFilterSelector")); //$NON-NLS-1$
    }

    @Override
    public void createFieldEditors() {
        addField(new ListEditor(IDEPreferenceConstants.P_CONDITIONAL_FILTERS,
                Localization.getString("ConditionalFilterPreferencePage.ConditionalFilters"), Localization.getString("ConditionalFilterPreferencePage.NewFilter"), "if()", new ConditionalExpressionValidator(), getFieldEditorParent())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    @Override
    public void init(IWorkbench workbench) {
    }
}
