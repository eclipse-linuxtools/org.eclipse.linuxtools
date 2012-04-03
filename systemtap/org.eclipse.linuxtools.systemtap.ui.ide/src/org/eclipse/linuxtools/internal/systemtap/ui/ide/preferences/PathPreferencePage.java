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
import org.eclipse.linuxtools.systemtap.ui.structures.ui.ListEditor;
import org.eclipse.linuxtools.systemtap.ui.structures.validators.DirectoryValidator;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;



public class PathPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	public PathPreferencePage() {
		super(GRID);
		LogManager.logDebug("Start PathPreferencePage:", this);
		setPreferenceStore(IDEPlugin.getDefault().getPreferenceStore());
		setDescription("Important directories");
		LogManager.logDebug("End PathPreferencePage:", this);
	}
	
	public void createFieldEditors() {
		LogManager.logDebug("Start createFieldEditors:", this);
//		addField(new DirectoryFieldEditor(IDEPreferenceConstants.P_DEFAULT_TAPSET, 
//				"&Default tapset directory:", getFieldEditorParent()));
		addField(new DirectoryFieldEditor(IDEPreferenceConstants.P_KERNEL_SOURCE, 
				Localization.getString("PathPreferencePage.KernelSourceDirectory"), getFieldEditorParent()));

		addField(new ListEditor(IDEPreferenceConstants.P_EXCLUDED_KERNEL_SOURCE,
				Localization.getString("PathPreferencePage.ExcludedSourceFolders"), Localization.getString("PathPreferencePage.ExcludedDirectory"), "", new DirectoryValidator(), getFieldEditorParent()));
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