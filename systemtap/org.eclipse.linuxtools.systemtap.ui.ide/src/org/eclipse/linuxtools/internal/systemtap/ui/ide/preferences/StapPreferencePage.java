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


public class StapPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	public StapPreferencePage() {
		super(GRID);
		LogManager.logDebug("Start StapPreferencePage:", this); //$NON-NLS-1$
		setPreferenceStore(IDEPlugin.getDefault().getPreferenceStore());
		setDescription(Localization.getString("StapPreferencePage.StapPreferenceDescription"));
		LogManager.logDebug("End StapPreferencePage:", this); //$NON-NLS-1$
	}
	
	//TODO: Add content to this page
	@Override
	public void createFieldEditors() {
		LogManager.logDebug("Start createFieldEditors:", this); //$NON-NLS-1$
		LogManager.logDebug("End createFieldEditors:", this); //$NON-NLS-1$
	}

	public void init(IWorkbench workbench) {
		LogManager.logInfo("Initializing", this); //$NON-NLS-1$
	}

	@Override
	public void dispose() {
		LogManager.logInfo("Disposing", this); //$NON-NLS-1$
		super.dispose();
	}
}
