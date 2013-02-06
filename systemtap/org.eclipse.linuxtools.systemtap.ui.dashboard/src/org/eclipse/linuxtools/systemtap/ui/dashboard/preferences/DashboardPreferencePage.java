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

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PathEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.linuxtools.systemtap.ui.dashboard.internal.DashboardPlugin;
import org.eclipse.linuxtools.systemtap.ui.dashboard.internal.Localization;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class DashboardPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	public DashboardPreferencePage() {
		super(GRID);
		setPreferenceStore(DashboardPlugin.getDefault().getPreferenceStore());
		setDescription(Localization.getString("DashboardPreferencePage.DashboardPreferences")); //$NON-NLS-1$
	}

	@Override
	public void createFieldEditors() {
		addField(new PathEditor(DashboardPreferenceConstants.P_MODULE_FOLDERS,
				Localization.getString("DashboardPreferencePage.AdditionalDirectories"), Localization.getString("DashboardPreferencePage.ModuleDirectory"), getFieldEditorParent())); //$NON-NLS-1$ //$NON-NLS-2$

		addField(new IntegerFieldEditor(DashboardPreferenceConstants.P_DASHBOARD_UPDATE_DELAY,
				Localization.getString("DashboardPreferencePage.RefreshDelay"), getFieldEditorParent())); //$NON-NLS-1$

		addField(new StringFieldEditor(DashboardPreferenceConstants.P_DASHBOARD_EXAMPLES_DIR,
				Localization.getString("DashboardPreferencePage.ExamplesDir"),  getFieldEditorParent())); //$NON-NLS-1$
	}

	@Override
	public void init(IWorkbench workbench) {
	}
}
