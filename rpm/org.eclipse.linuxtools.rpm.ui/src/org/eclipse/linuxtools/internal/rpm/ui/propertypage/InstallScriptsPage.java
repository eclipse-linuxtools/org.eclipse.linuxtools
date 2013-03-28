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

	@Override
	protected void addFields(Composite composite) {

		// RPM labels and text fields setup
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);

		Label rpmPreInstallLabel = new Label(composite, SWT.NONE);
		rpmPreInstallLabel.setText(Messages
				.getString("InstallScriptsPage.PreinstallScript")); //$NON-NLS-1$
		Text rpmPreInstallText = new Text(composite, SWT.MULTI | SWT.BORDER
				| SWT.READ_ONLY | SWT.V_SCROLL | SWT.WRAP);
		rpmPreInstallText.setLayoutData(gd);

		Label rpmPostInstallLabel = new Label(composite, SWT.NONE);
		rpmPostInstallLabel.setText(Messages
				.getString("InstallScriptsPage.PostinstallScript")); //$NON-NLS-1$
		Text rpmPostInstallText = new Text(composite, SWT.MULTI | SWT.BORDER
				| SWT.READ_ONLY | SWT.V_SCROLL | SWT.WRAP);
		rpmPostInstallText.setLayoutData(gd);

		Label rpmPreUnInstallLabel = new Label(composite, SWT.NONE);
		rpmPreUnInstallLabel.setText(Messages
				.getString("InstallScriptsPage.PreuninstallScript")); //$NON-NLS-1$
		Text rpmPreUnInstallText = new Text(composite, SWT.MULTI | SWT.BORDER
				| SWT.READ_ONLY | SWT.V_SCROLL | SWT.WRAP);
		rpmPreUnInstallText.setLayoutData(gd);

		Label rpmPostUnInstallLabel = new Label(composite, SWT.NONE);
		rpmPostUnInstallLabel.setText(Messages
				.getString("InstallScriptsPage.PostuninstallScript")); //$NON-NLS-1$
		Text rpmPostUnInstallText = new Text(composite, SWT.MULTI | SWT.BORDER
				| SWT.READ_ONLY | SWT.V_SCROLL | SWT.WRAP);
		rpmPostUnInstallText.setLayoutData(gd);

		// Populate RPM text fields
		try {
			IFile rpmFile = (IFile) getElement();
			String rpm_PreInstall = RPMQuery.getPreInstallScript(rpmFile);
			rpmPreInstallText.setText(rpm_PreInstall);

			String rpm_PostInstall = RPMQuery.getPostInstallScript(rpmFile);
			rpmPostInstallText.setText(rpm_PostInstall);

			String rpm_PreUnInstall = RPMQuery.getPreUninstallScript(rpmFile);
			rpmPreUnInstallText.setText(rpm_PreUnInstall);

			String rpm_PostUnInstall = RPMQuery.getPostUninstallScript(rpmFile);
			rpmPostUnInstallText.setText(rpm_PostUnInstall);
		} catch (CoreException e) {
			StatusManager.getManager().handle(new StatusAdapter(e.getStatus()),
					StatusManager.LOG | StatusManager.SHOW);
		}

	}

}