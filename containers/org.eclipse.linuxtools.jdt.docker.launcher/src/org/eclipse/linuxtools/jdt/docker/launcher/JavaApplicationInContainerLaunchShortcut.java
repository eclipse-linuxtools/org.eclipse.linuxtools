/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.ui.launchConfigurations.JavaLaunchShortcut#getConfigurationType()
	 */
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
			wc.setAttribute("org.eclipse.linuxtools.jdt.docker.launcher.connection.uri", isd.getConnection().getUri()); //$NON-NLS-1$
			wc.setAttribute("org.eclipse.linuxtools.jdt.docker.launcher.image.id", isd.getImage().id()); //$NON-NLS-1$
			wc.doSave();
		} catch (CoreException e) {
		}

		return cfg;
	}
}
