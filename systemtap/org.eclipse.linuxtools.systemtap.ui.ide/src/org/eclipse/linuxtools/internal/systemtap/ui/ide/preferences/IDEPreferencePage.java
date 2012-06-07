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


public class IDEPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	public IDEPreferencePage() {
		super(GRID);
		LogManager.logDebug("Start IDEPreferencePage:", this); //$NON-NLS-1$
		setPreferenceStore(IDEPlugin.getDefault().getPreferenceStore());
		setDescription("Preferences when loading the IDE perspective");
		LogManager.logDebug("End IDEPreferencePage:", this); //$NON-NLS-1$
	}
	
	public void createFieldEditors() {
		LogManager.logDebug("Start createFieldEditors:", this); //$NON-NLS-1$
		addField(new BooleanFieldEditor(
				IDEPreferenceConstants.P_STORED_TREE, 
				Localization.getString("IDEPreferencePage.UseStoredTapsetTree"),
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
