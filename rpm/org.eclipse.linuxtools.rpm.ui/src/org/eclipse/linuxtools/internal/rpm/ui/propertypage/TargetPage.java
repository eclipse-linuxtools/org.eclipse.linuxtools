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
 * Property page displaying the target (aka build arch and friends) as fetched from the RPM headers.
 *
 */
public class TargetPage extends AbstractRPMPropertyPage {

	private static final String RPM_ARCH = Messages
			.getString("TargetPage.Architecture"); //$NON-NLS-1$

	private static final String RPM_PLATFORM = Messages
			.getString("TargetPage.Platform"); //$NON-NLS-1$

	private static final String RPM_OS = Messages.getString("TargetPage.OS"); //$NON-NLS-1$

	private static final String RPM_HOST = Messages
			.getString("TargetPage.BuildHost"); //$NON-NLS-1$

	private static final String RPM_TIME = Messages
			.getString("TargetPage.BuildTime"); //$NON-NLS-1$

	private static final int ARCH_FIELD_WIDTH = 8;

	private static final int PLATFORM_FIELD_WIDTH = 20;

	private static final int OS_FIELD_WIDTH = 10;

	private static final int HOST_FIELD_WIDTH = 40;

	private static final int TIME_FIELD_WIDTH = 35;

	private Label rpm_archText;

	private Label rpm_platformText;

	private Label rpm_osText;

	private Label rpm_hostText;

	private Label rpm_timeText;

	@Override
	protected void addFields(Composite parent) {
		Composite composite = createDefaultComposite(parent);

		// RPM labels and text fields setup

		Label rpmArchLabel = new Label(composite, SWT.NONE);
		rpmArchLabel.setText(RPM_ARCH);
		rpm_archText = new Label(composite, SWT.HORIZONTAL);
		GridData gdArch = new GridData();
		gdArch.widthHint = convertWidthInCharsToPixels(ARCH_FIELD_WIDTH);
		rpm_archText.setLayoutData(gdArch);

		Label rpmPlatformLabel = new Label(composite, SWT.NONE);
		rpmPlatformLabel.setText(RPM_PLATFORM);
		rpm_platformText = new Label(composite, SWT.HORIZONTAL);
		GridData gdPlatform = new GridData();
		gdPlatform.widthHint = convertWidthInCharsToPixels(PLATFORM_FIELD_WIDTH);
		rpm_platformText.setLayoutData(gdPlatform);

		Label rpmOSLabel = new Label(composite, SWT.NONE);
		rpmOSLabel.setText(RPM_OS);
		rpm_osText = new Label(composite, SWT.HORIZONTAL);
		GridData gdOS = new GridData();
		gdOS.widthHint = convertWidthInCharsToPixels(OS_FIELD_WIDTH);
		rpm_osText.setLayoutData(gdOS);

		Label rpmHostLabel = new Label(composite, SWT.NONE);
		rpmHostLabel.setText(RPM_HOST);
		rpm_hostText = new Label(composite, SWT.HORIZONTAL);
		GridData gdHost = new GridData();
		gdHost.widthHint = convertWidthInCharsToPixels(HOST_FIELD_WIDTH);
		rpm_hostText.setLayoutData(gdHost);

		Label rpmTimeLabel = new Label(composite, SWT.NONE);
		rpmTimeLabel.setText(RPM_TIME);
		rpm_timeText = new Label(composite, SWT.HORIZONTAL);
		GridData gdTime = new GridData();
		gdTime.widthHint = convertWidthInCharsToPixels(TIME_FIELD_WIDTH);
		rpm_timeText.setLayoutData(gdTime);

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