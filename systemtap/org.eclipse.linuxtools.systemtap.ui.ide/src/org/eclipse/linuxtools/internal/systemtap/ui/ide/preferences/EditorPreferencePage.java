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

import org.eclipse.jface.preference.*;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.IDEPlugin;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.Localization;
import org.eclipse.linuxtools.systemtap.ui.logging.LogManager;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;


public class EditorPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	public EditorPreferencePage() {
		super(GRID);
		LogManager.logDebug("Start EditorPreferencePage:", this);
		setPreferenceStore(IDEPlugin.getDefault().getPreferenceStore());
		setDescription(Localization.getString("EditorPreferencePage.EditorPreferenceDescription"));
		LogManager.logDebug("End EditorPreferencePage:", this);
	}
	
	public void createFieldEditors() {
		LogManager.logDebug("Start createFieldEditors:", this);
		addField(new ColorFieldEditor(
				IDEPreferenceConstants.P_EDITOR_BACKGROUND, 
				Localization.getString("EditorPreferencePage.BackgroundColor"), getFieldEditorParent()));
		
		addField(new BooleanFieldEditor(
				IDEPreferenceConstants.P_SHOW_LINE_NUMBERS,
				Localization.getString("EditorPreferencePage.ShowLineNumbers"),
				getFieldEditorParent()));
		LogManager.logDebug("End createFieldEditors:", this);
	}

	public void init(IWorkbench workbench) {
		LogManager.logInfo("Initializing", this);
	}

	public void dispose() {
		LogManager.logInfo("Disposing", this);
		super.dispose();
	}
}