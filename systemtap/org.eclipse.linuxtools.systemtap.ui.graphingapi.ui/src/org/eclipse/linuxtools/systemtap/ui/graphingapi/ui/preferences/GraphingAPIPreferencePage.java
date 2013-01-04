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

package org.eclipse.linuxtools.systemtap.ui.graphingapi.ui.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.linuxtools.internal.systemtap.ui.graphingapi.ui.GraphingAPIUIPlugin;
import org.eclipse.linuxtools.internal.systemtap.ui.graphingapi.ui.Localization;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;



public class GraphingAPIPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	public GraphingAPIPreferencePage() {
		super(GRID);
		setPreferenceStore(GraphingAPIUIPlugin.getDefault().getPreferenceStore());
		setDescription(Localization.getString("GraphingAPIPreferencePage.GraphDisplayPreferences")); //$NON-NLS-1$
	}
	
	@Override
	public void createFieldEditors() {
	}

	public void init(IWorkbench workbench) {}
	
}