/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse, Anithra P J
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.ui.dashboard.preferences;

import org.eclipse.jface.preference.*;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;

import org.eclipse.linuxtools.systemtap.ui.logging.LogManager;
import org.eclipse.linuxtools.systemtap.ui.dashboard.internal.DashboardPlugin;
import org.eclipse.linuxtools.systemtap.ui.dashboard.internal.Localization;

public class DashboardPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	public DashboardPreferencePage() {
		super(GRID);
		LogManager.logDebug("Start DashboardPreferencePage:", this); //$NON-NLS-1$
		setPreferenceStore(DashboardPlugin.getDefault().getPreferenceStore());
		setDescription(Localization.getString("DashboardPreferencePage.DashboardPreferences"));
		LogManager.logDebug("End DashboardPreferencePage:", this); //$NON-NLS-1$
	}
	
	public void createFieldEditors() {
		LogManager.logDebug("Start createFieldEditors:", this); //$NON-NLS-1$
		addField(new PathEditor(DashboardPreferenceConstants.P_MODULE_FOLDERS,
				Localization.getString("DashboardPreferencePage.AdditionalDirectories"), Localization.getString("DashboardPreferencePage.ModuleDirectory"), getFieldEditorParent()));
		
		addField(new IntegerFieldEditor(DashboardPreferenceConstants.P_DASHBOARD_UPDATE_DELAY,
				Localization.getString("DashboardPreferencePage.RefreshDelay"), getFieldEditorParent()));
		
		addField(new StringFieldEditor(DashboardPreferenceConstants.P_DASHBOARD_EXAMPLES_DIR,
				Localization.getString("DashboardPreferencePage.ExamplesDir"),  getFieldEditorParent()));
		
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
