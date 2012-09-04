/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Anithra P J
 *     Alexander Kurtakov- make it jface dialog
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.dashboardextension.dialogs;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.linuxtools.systemtap.ui.dashboard.internal.DashboardPlugin;
import org.eclipse.linuxtools.systemtap.ui.dashboard.preferences.DashboardPreferenceConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class ScriptDetails extends TitleAreaDialog {
	private DirectoryFieldEditor dirText;
	private FileFieldEditor scriptText;

	public ScriptDetails(Shell parent) {
		super(parent);
	}

	@Override
	public void create() {
		super.create();
		setTitle(Messages.ScriptDetails_Title);
		setMessage(Messages.ScriptDetails_Choose_Directory);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		super.createDialogArea(parent);
		Composite newParent = new Composite(parent, SWT.NULL);
		newParent.setLayoutData(new GridData(GridData.FILL_BOTH));

		dirText = new DirectoryFieldEditor("", Messages.ScriptDetails_Directory, newParent); //$NON-NLS-1$
		dirText.setStringValue((DashboardPlugin.getDefault()
				.getPreferenceStore()
				.getString(DashboardPreferenceConstants.P_DASHBOARD_EXAMPLES_DIR)));

		scriptText = new FileFieldEditor("", Messages.ScriptDetails_Script, newParent); //$NON-NLS-1$
		return newParent;
	}

	@Override
	protected void okPressed() {
		DashboardPlugin
				.getDefault()
				.getPreferenceStore()
				.setValue(
						DashboardPreferenceConstants.P_DASHBOARD_EXAMPLES_DIR,
						dirText.getStringValue());
		DashboardPlugin
				.getDefault()
				.getPreferenceStore()
				.setValue(DashboardPreferenceConstants.P_DASHBOARD_SCRIPT,
						scriptText.getStringValue());
		super.okPressed();
	}

}
