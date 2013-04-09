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

package org.eclipse.linuxtools.internal.systemtap.ui.graphing.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.linuxtools.internal.systemtap.ui.graphing.GraphingPlugin;
import org.eclipse.linuxtools.internal.systemtap.ui.graphing.Localization;
import org.eclipse.linuxtools.systemtap.graphingapi.ui.preferences.GraphingAPIPreferenceConstants;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;



public class DataTablePreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	public DataTablePreferencePage() {
		super(GRID);
		setPreferenceStore(GraphingPlugin.getDefault().getPreferenceStore());
		setDescription(Localization.getString("DataTablePreferencePage.GraphDisplayPreferences")); //$NON-NLS-1$
	}

	@Override
	public void createFieldEditors() {
		addField(new BooleanFieldEditor(
				GraphingAPIPreferenceConstants.P_JUMP_NEW_TABLE_ENTRY,
				Localization.getString("DataTablePreferencePage.JumpNewestEntry"), //$NON-NLS-1$
				getFieldEditorParent()));

		addField(new BooleanFieldEditor(
				GraphingAPIPreferenceConstants.P_AUTO_RESIZE,
				Localization.getString("DataTablePreferencePage.AutoResizeColumns"), //$NON-NLS-1$
				getFieldEditorParent()));

		addField(
				new IntegerFieldEditor(
				GraphingAPIPreferenceConstants.P_MAX_DATA_ITEMS,
				Localization.getString("DataTablePreferencePage.MaxDataItems"), //$NON-NLS-1$
				getFieldEditorParent()));
	}

	@Override
	public void init(IWorkbench workbench) {
	}

}

