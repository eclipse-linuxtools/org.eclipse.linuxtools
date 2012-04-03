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


public class StapOptionsPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	public StapOptionsPreferencePage() {
		super(GRID);
		LogManager.logDebug("Start StapOptionsPreferencePage:", this);
		setPreferenceStore(IDEPlugin.getDefault().getPreferenceStore());
		setDescription(Localization.getString("StapOptionsPreferencePage.StapOptions"));
		LogManager.logDebug("End StapOptionsPreferencePage:", this);
	}

	public void createFieldEditors() {
		LogManager.logDebug("Start createFieldEditors:", this);
		for(int i=0; i<IDEPreferenceConstants.P_STAP.length; i++) {
			addField(
					new BooleanFieldEditor(
						IDEPreferenceConstants.P_STAP[i][2],
						IDEPreferenceConstants.P_STAP[i][0] + "\t" + IDEPreferenceConstants.P_STAP[i][1],
						getFieldEditorParent()));
			
			if(2 < IDEPreferenceConstants.P_STAP[i][0].length()) {
				StringFieldEditor sfe = new StringFieldEditor(
					IDEPreferenceConstants.P_STAP_OPTS[i-IDEPreferenceConstants.P_STAP.length+IDEPreferenceConstants.P_STAP_OPTS.length],
					"\t",
					getFieldEditorParent());
				
				addField(sfe);
			}
		}
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