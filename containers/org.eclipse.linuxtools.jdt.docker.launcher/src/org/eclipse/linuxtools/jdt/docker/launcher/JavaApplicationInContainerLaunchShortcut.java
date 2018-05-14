/*******************************************************************************
 * Copyright (c) 2017, 2018 Red Hat Inc. and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.jdt.docker.launcher;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaApplicationLaunchShortcut;

public class JavaApplicationInContainerLaunchShortcut extends JavaApplicationLaunchShortcut {

	@Override
	protected ILaunchConfigurationType getConfigurationType() {
		return DebugPlugin.getDefault().getLaunchManager()
				.getLaunchConfigurationType("org.eclipse.linuxtools.jdt.docker.launcher.JavaAppInContainerLaunchConfigurationType"); //$NON-NLS-1$
	}

	@Override
	protected ILaunchConfiguration createConfiguration(IType type) {
		ImageSelectionDialog isd = new ImageSelectionDialog();
		if (isd.open() != 0) {
			return null;
		}

		ILaunchConfiguration cfg = super.createConfiguration(type);
		try {
			ILaunchConfigurationWorkingCopy wc = cfg.getWorkingCopy();
			wc.setAttribute(JavaLaunchConfigurationConstants.CONNECTION_URI, isd.getConnection().getUri());
			wc.setAttribute(JavaLaunchConfigurationConstants.IMAGE_ID, isd.getImage().id());
			wc.doSave();
		} catch (CoreException e) {
		}

		return cfg;
	}
}
