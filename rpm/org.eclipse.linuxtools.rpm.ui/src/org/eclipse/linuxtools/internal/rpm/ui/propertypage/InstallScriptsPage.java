/*******************************************************************************
 * Copyright (c) 2004-2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.ui.propertypage;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.rpm.core.utils.RPMQuery;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.statushandlers.StatusAdapter;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * Property page displaying install scripts as fetched by RPM headers.
 *
 */
public class InstallScriptsPage extends AbstractRPMPropertyPage {

	private static final String RPM_PRE_INSTALL = Messages
			.getString("InstallScriptsPage.PreinstallScript"); //$NON-NLS-1$

	private static final String RPM_POST_INSTALL = Messages
			.getString("InstallScriptsPage.PostinstallScript"); //$NON-NLS-1$

	private static final String RPM_PRE_UNINSTALL = Messages
			.getString("InstallScriptsPage.PreuninstallScript"); //$NON-NLS-1$

	private static final String RPM_POST_UNINSTALL = Messages
			.getString("InstallScriptsPage.PostuninstallScript"); //$NON-NLS-1$

	private static final int SCRIPT_ENTRIES_FIELD_WIDTH = 80;

	private static final int SCRIPT_ENTRIES_FIELD_HEIGHT = 20;

	private Text rpm_PreInstallText;

	private Text rpm_PostInstallText;

	private Text rpm_PreUnInstallText;

	private Text rpm_PostUnInstallText;

	@Override
	protected void addFields(Composite parent) {
		Composite composite = createDefaultComposite(parent);

		// RPM labels and text fields setup

		Label rpmPreInstallLabel = new Label(composite, SWT.NONE);
		rpmPreInstallLabel.setText(RPM_PRE_INSTALL);
		rpm_PreInstallText = new Text(composite, SWT.MULTI | SWT.BORDER
				| SWT.READ_ONLY | SWT.V_SCROLL | SWT.WRAP);
		GridData gdPreInst = new GridData();
		gdPreInst.widthHint = convertWidthInCharsToPixels(SCRIPT_ENTRIES_FIELD_WIDTH);
		gdPreInst.heightHint = convertWidthInCharsToPixels(SCRIPT_ENTRIES_FIELD_HEIGHT);
		rpm_PreInstallText.setLayoutData(gdPreInst);

		Label rpmPostInstallLabel = new Label(composite, SWT.NONE);
		rpmPostInstallLabel.setText(RPM_POST_INSTALL);
		rpm_PostInstallText = new Text(composite, SWT.MULTI | SWT.BORDER
				| SWT.READ_ONLY | SWT.V_SCROLL | SWT.WRAP);
		GridData gdPostInst = new GridData();
		gdPostInst.widthHint = convertWidthInCharsToPixels(SCRIPT_ENTRIES_FIELD_WIDTH);
		gdPostInst.heightHint = convertWidthInCharsToPixels(SCRIPT_ENTRIES_FIELD_HEIGHT);
		rpm_PostInstallText.setLayoutData(gdPostInst);

		Label rpmPreUnInstallLabel = new Label(composite, SWT.NONE);
		rpmPreUnInstallLabel.setText(RPM_PRE_UNINSTALL);
		rpm_PreUnInstallText = new Text(composite, SWT.MULTI | SWT.BORDER
				| SWT.READ_ONLY | SWT.V_SCROLL | SWT.WRAP);
		GridData gdPreUnInst = new GridData();
		gdPreUnInst.widthHint = convertWidthInCharsToPixels(SCRIPT_ENTRIES_FIELD_WIDTH);
		gdPreUnInst.heightHint = convertWidthInCharsToPixels(SCRIPT_ENTRIES_FIELD_HEIGHT);
		rpm_PreUnInstallText.setLayoutData(gdPreUnInst);

		Label rpmPostUnInstallLabel = new Label(composite, SWT.NONE);
		rpmPostUnInstallLabel.setText(RPM_POST_UNINSTALL);
		rpm_PostUnInstallText = new Text(composite, SWT.MULTI | SWT.BORDER
				| SWT.READ_ONLY | SWT.V_SCROLL | SWT.WRAP);
		GridData gdPostUnInst = new GridData();
		gdPostUnInst.widthHint = convertWidthInCharsToPixels(SCRIPT_ENTRIES_FIELD_WIDTH);
		gdPostUnInst.heightHint = convertWidthInCharsToPixels(SCRIPT_ENTRIES_FIELD_HEIGHT);
		rpm_PostUnInstallText.setLayoutData(gdPostUnInst);

		// Populate RPM text fields
		try {
			IFile rpmFile = (IFile) getElement();
			String rpm_PreInstall = RPMQuery.getPreInstallScript(rpmFile);
			rpm_PreInstallText.setText(rpm_PreInstall);

			String rpm_PostInstall = RPMQuery.getPostInstallScript(rpmFile);
			rpm_PostInstallText.setText(rpm_PostInstall);

			String rpm_PreUnInstall = RPMQuery.getPreUninstallScript(rpmFile);
			rpm_PreUnInstallText.setText(rpm_PreUnInstall);

			String rpm_PostUnInstall = RPMQuery.getPostUninstallScript(rpmFile);
			rpm_PostUnInstallText.setText(rpm_PostUnInstall);
		} catch (CoreException e) {
			StatusManager.getManager().handle(new StatusAdapter(e.getStatus()),
					StatusManager.LOG | StatusManager.SHOW);
		}

	}

}