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


public class TapsetsPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	public TapsetsPreferencePage() {
		super(GRID);
		LogManager.logDebug("Start TapsetsPreferencePage:", this);
		setPreferenceStore(IDEPlugin.getDefault().getPreferenceStore());
		setDescription(Localization.getString("TapsetsPreferencePage.AdditionalTapsetsCategory"));
		LogManager.logDebug("End TapsetsPreferencePage:", this);
	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {
		LogManager.logDebug("Start createFieldEditors:", this);
		addField(new PathEditor(IDEPreferenceConstants.P_TAPSETS,
				Localization.getString("TapsetsPreferencePage.AdditionalTapsets"), Localization.getString("TapsetsPreferencePage.TapsetDirectory"), getFieldEditorParent()));
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