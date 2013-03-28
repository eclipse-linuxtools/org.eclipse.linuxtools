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
		Label rpmArchText = new Label(composite, SWT.NONE);
		rpmArchText.setLayoutData(gd);

		Label rpmPlatformLabel = new Label(composite, SWT.NONE);
		rpmPlatformLabel.setText(Messages.getString("TargetPage.Platform")); //$NON-NLS-1$
		Label rpmPlatformText = new Label(composite, SWT.NONE);
		rpmPlatformText.setLayoutData(gd);

		Label rpmOSLabel = new Label(composite, SWT.NONE);
		rpmOSLabel.setText(Messages.getString("TargetPage.OS")); //$NON-NLS-1$
		Label rpmOsText = new Label(composite, SWT.NONE);
		rpmOsText.setLayoutData(gd);

		Label rpmHostLabel = new Label(composite, SWT.NONE);
		rpmHostLabel.setText(Messages.getString("TargetPage.BuildHost")); //$NON-NLS-1$
		Label rpmHostText = new Label(composite, SWT.NONE);
		rpmHostText.setLayoutData(gd);

		Label rpmTimeLabel = new Label(composite, SWT.NONE);
		rpmTimeLabel.setText(Messages.getString("TargetPage.BuildTime")); //$NON-NLS-1$
		Label rpmTimeText = new Label(composite, SWT.NONE);
		rpmTimeText.setLayoutData(gd);

		// Populate RPM text fields
		try {
			IFile rpmFile = (IFile) getElement();
			String rpmArch = RPMQuery.getArch(rpmFile);
			rpmArchText.setText(rpmArch);
			String rpmPlatform = RPMQuery.getPlatform(rpmFile);
			rpmPlatformText.setText(rpmPlatform);
			String rpmOs = RPMQuery.getOS(rpmFile);
			rpmOsText.setText(rpmOs);
			String rpmHost = RPMQuery.getBuildHost(rpmFile);
			rpmHostText.setText(rpmHost);
			String rpmTime = RPMQuery.getBuildTime(rpmFile);
			rpmTimeText.setText(rpmTime);
		} catch (CoreException e) {
			StatusManager.getManager().handle(new StatusAdapter(e.getStatus()),
					StatusManager.LOG | StatusManager.SHOW);
		}

	}

}