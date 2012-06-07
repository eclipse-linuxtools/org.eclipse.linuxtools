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

package org.eclipse.linuxtools.systemtap.ui.systemtapgui.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.linuxtools.internal.systemtap.ui.systemtapgui.Localization;
import org.eclipse.linuxtools.internal.systemtap.ui.systemtapgui.SystemTapGUIPlugin;
import org.eclipse.linuxtools.systemtap.ui.logging.LogManager;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;



public class SystemTapPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	
	/**
	 * Get the current preferences.
	 */
	public SystemTapPreferencePage() {
		super(GRID);
		LogManager.logDebug("Start SystemTapPreferencePage:", this); //$NON-NLS-1$
		setPreferenceStore(SystemTapGUIPlugin.getDefault().getPreferenceStore());
		setDescription(Localization.getString("SystemTapPreferencePage.GUIPreferencesDescription"));
		LogManager.logDebug("End SystemTapPreferencePage:", this); //$NON-NLS-1$
	}
	
	/**
	 * Sets up the field editors for optional change by the user.
	 */
	public void createFieldEditors() {
		LogManager.logDebug("Start createFieldEditors:", this); //$NON-NLS-1$
		addField(new BooleanFieldEditor(
				PreferenceConstants.P_WINDOW_STATE, 
				Localization.getString("SystemTapPreferencePage.RememberWindowState"),
				getFieldEditorParent()));
		LogManager.logDebug("End createFieldEditors:", this); //$NON-NLS-1$
	}

	public void init(IWorkbench workbench) {
		LogManager.logInfo("Initializing", this); //$NON-NLS-1$
	}

	public void dispose() {
		LogManager.logInfo("Disposing", this); //$NON-NLS-1$
		super.dispose();
	}
}
