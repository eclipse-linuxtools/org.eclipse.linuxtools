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

package org.eclipse.linuxtools.internal.systemtap.graphing.ui.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.linuxtools.internal.systemtap.graphing.ui.GraphingUIPlugin;
import org.eclipse.linuxtools.internal.systemtap.graphing.ui.Localization;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;



public class GraphPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	public GraphPreferencePage() {
		super(GRID);
		setPreferenceStore(GraphingUIPlugin.getDefault().getPreferenceStore());
		setDescription(Localization.getString("GraphPreferencePage.GraphDisplayPreferences")); //$NON-NLS-1$
	}

	@Override
	public void createFieldEditors() {
		addField(new BooleanFieldEditor(
				GraphingPreferenceConstants.P_SHOW_X_GRID_LINES,
				Localization.getString("GraphPreferencePage.ShowXGridLines"), //$NON-NLS-1$
				getFieldEditorParent()));

		addField(new BooleanFieldEditor(
				GraphingPreferenceConstants.P_SHOW_Y_GRID_LINES,
				Localization.getString("GraphPreferencePage.ShowYGridLines"), //$NON-NLS-1$
				getFieldEditorParent()));

		addField(
				new IntegerFieldEditor(
				GraphingPreferenceConstants.P_VIEWABLE_DATA_ITEMS,
				Localization.getString("GraphPreferencePage.ViewableDataItems"), //$NON-NLS-1$
				getFieldEditorParent()));

		addField(
				new IntegerFieldEditor(
				GraphingPreferenceConstants.P_X_SERIES_TICKS,
				Localization.getString("GraphPreferencePage.XSeriesTicks"), //$NON-NLS-1$
				getFieldEditorParent()));

		addField(
				new IntegerFieldEditor(
				GraphingPreferenceConstants.P_Y_SERIES_TICKS,
				Localization.getString("GraphPreferencePage.YSeriesTicks"), //$NON-NLS-1$
				getFieldEditorParent()));
	}

	@Override
	public void init(IWorkbench workbench) {}

}

