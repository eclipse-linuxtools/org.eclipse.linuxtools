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
import org.eclipse.linuxtools.systemtap.ui.graphingapi.ui.preferences.GraphingAPIPreferenceConstants;
import org.eclipse.linuxtools.systemtap.ui.logging.LogManager;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;



public class GraphPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	public GraphPreferencePage() {
		super(GRID);
		LogManager.logDebug("Start GraphPreferencePage:", this);
		setPreferenceStore(GraphingPlugin.getDefault().getPreferenceStore());
		//setPreferenceStore(GraphingAPIUIPlugin.getDefault().getPreferenceStore());
		setDescription(Localization.getString("GraphPreferencePage.GraphDisplayPreferences"));
		LogManager.logDebug("End GraphPreferencePage:", this);
	}
	
	public void createFieldEditors() {
		LogManager.logDebug("Start createFieldEditors:", this);

		addField(new BooleanFieldEditor(
				GraphingAPIPreferenceConstants.P_SHOW_X_GRID_LINES,
				Localization.getString("GraphPreferencePage.ShowXGridLines"),
				getFieldEditorParent()));

		addField(new BooleanFieldEditor(
				GraphingAPIPreferenceConstants.P_SHOW_Y_GRID_LINES,
				Localization.getString("GraphPreferencePage.ShowYGridLines"),
				getFieldEditorParent()));

		addField(
				new IntegerFieldEditor(
				GraphingAPIPreferenceConstants.P_MAX_DATA_ITEMS,
				Localization.getString("GraphPreferencePage.MaxDataItems"),
				getFieldEditorParent()));

		addField(
				new IntegerFieldEditor(
				GraphingAPIPreferenceConstants.P_VIEWABLE_DATA_ITEMS,
				Localization.getString("GraphPreferencePage.ViewableDataItems"),
				getFieldEditorParent()));

		addField(
				new IntegerFieldEditor(
				GraphingAPIPreferenceConstants.P_X_SERIES_TICKS,
				Localization.getString("GraphPreferencePage.XSeriesTicks"),
				getFieldEditorParent()));

		addField(
				new IntegerFieldEditor(
				GraphingAPIPreferenceConstants.P_Y_SERIES_TICKS,
				Localization.getString("GraphPreferencePage.YSeriesTicks"),
				getFieldEditorParent()));
		
		LogManager.logDebug("End createFieldEditors:", this);
	}

	public void init(IWorkbench workbench) {
		LogManager.logDebug("Start init:", this);
		LogManager.logInfo("Initializing", this);
		LogManager.logDebug("End init:", this);
	}
	
	public void dispose() {
		LogManager.logDebug("Start dispose:", this);
		LogManager.logInfo("Disposing", this);
		super.dispose();
		LogManager.logDebug("End dispose:", this);
	}
}

