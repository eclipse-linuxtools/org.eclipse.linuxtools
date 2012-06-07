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

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.linuxtools.internal.systemtap.ui.graphing.GraphingPlugin;
import org.eclipse.linuxtools.internal.systemtap.ui.graphing.Localization;
import org.eclipse.linuxtools.systemtap.ui.logging.LogManager;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;



public class GraphingPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	public GraphingPreferencePage() {
		super(GRID);
		LogManager.logDebug("Start GraphingPreferencePage:", this); //$NON-NLS-1$
		setPreferenceStore(GraphingPlugin.getDefault().getPreferenceStore());
		setDescription(Localization.getString("GraphingPreferencePage.GraphDisplayPreferences"));
		LogManager.logDebug("End GraphingPreferencePage:", this); //$NON-NLS-1$
	}
	
	public void createFieldEditors() {
		LogManager.logDebug("Start createFieldEditors:", this); //$NON-NLS-1$

		addField(
				new IntegerFieldEditor(
				GraphingPreferenceConstants.P_GRAPH_UPDATE_DELAY,
				Localization.getString("GraphingPreferencePage.RefreshDelay"),
				getFieldEditorParent()));
		
		LogManager.logDebug("End createFieldEditors:", this); //$NON-NLS-1$
	}

	public void init(IWorkbench workbench) {
		LogManager.logDebug("Start init:", this); //$NON-NLS-1$
		LogManager.logInfo("Initializing", this); //$NON-NLS-1$
		LogManager.logDebug("End init:", this); //$NON-NLS-1$
	}
	
	public void dispose() {
		LogManager.logDebug("Start dispose:", this); //$NON-NLS-1$
		LogManager.logInfo("Disposing", this); //$NON-NLS-1$
		super.dispose();
		LogManager.logDebug("End dispose:", this); //$NON-NLS-1$
	}
}
