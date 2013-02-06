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
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;



public class GraphingPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	public GraphingPreferencePage() {
		super(GRID);
		setPreferenceStore(GraphingPlugin.getDefault().getPreferenceStore());
		setDescription(Localization.getString("GraphingPreferencePage.GraphDisplayPreferences")); //$NON-NLS-1$
	}
	
	@Override
	public void createFieldEditors() {
		addField(
				new IntegerFieldEditor(
				GraphingPreferenceConstants.P_GRAPH_UPDATE_DELAY,
				Localization.getString("GraphingPreferencePage.RefreshDelay"), //$NON-NLS-1$
				getFieldEditorParent()));
	}

	@Override
	public void init(IWorkbench workbench) {
	}
	
}
