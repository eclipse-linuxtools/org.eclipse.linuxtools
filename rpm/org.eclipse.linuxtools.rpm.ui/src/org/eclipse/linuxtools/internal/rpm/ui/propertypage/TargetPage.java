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
import org.eclipse.ui.statushandlers.StatusAdapter;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * Property page displaying the target (aka build arch and friends) as fetched
 * from the RPM headers.
 * 
 */
public class TargetPage extends AbstractRPMPropertyPage {

	@Override
	protected void addFields(Composite composite) {
		// RPM labels and text fields setup
		GridData gd = new GridData(SWT.FILL, SWT.NONE, true, false);

		Label rpmArchLabel = new Label(composite, SWT.NONE);
		rpmArchLabel.setText(Messages.getString("TargetPage.Architecture")); //$NON-NLS-1$
		Label rpm_archText = new Label(composite, SWT.NONE);
		rpm_archText.setLayoutData(gd);

		Label rpmPlatformLabel = new Label(composite, SWT.NONE);
		rpmPlatformLabel.setText(Messages.getString("TargetPage.Platform")); //$NON-NLS-1$
		Label rpm_platformText = new Label(composite, SWT.NONE);
		rpm_platformText.setLayoutData(gd);

		Label rpmOSLabel = new Label(composite, SWT.NONE);
		rpmOSLabel.setText(Messages.getString("TargetPage.OS")); //$NON-NLS-1$
		Label rpm_osText = new Label(composite, SWT.NONE);
		rpm_osText.setLayoutData(gd);

		Label rpmHostLabel = new Label(composite, SWT.NONE);
		rpmHostLabel.setText(Messages.getString("TargetPage.BuildHost")); //$NON-NLS-1$
		Label rpm_hostText = new Label(composite, SWT.NONE);
		rpm_hostText.setLayoutData(gd);

		Label rpmTimeLabel = new Label(composite, SWT.NONE);
		rpmTimeLabel.setText(Messages.getString("TargetPage.BuildTime")); //$NON-NLS-1$
		Label rpm_timeText = new Label(composite, SWT.NONE);
		rpm_timeText.setLayoutData(gd);

		// Populate RPM text fields
		try {
			IFile rpmFile = (IFile) getElement();
			String rpm_arch = RPMQuery.getArch(rpmFile);
			rpm_archText.setText(rpm_arch);
			String rpm_platform = RPMQuery.getPlatform(rpmFile);
			rpm_platformText.setText(rpm_platform);
			String rpm_os = RPMQuery.getOS(rpmFile);
			rpm_osText.setText(rpm_os);
			String rpm_host = RPMQuery.getBuildHost(rpmFile);
			rpm_hostText.setText(rpm_host);
			String rpm_time = RPMQuery.getBuildTime(rpmFile);
			rpm_timeText.setText(rpm_time);
		} catch (CoreException e) {
			StatusManager.getManager().handle(new StatusAdapter(e.getStatus()),
					StatusManager.LOG | StatusManager.SHOW);
		}

	}

}