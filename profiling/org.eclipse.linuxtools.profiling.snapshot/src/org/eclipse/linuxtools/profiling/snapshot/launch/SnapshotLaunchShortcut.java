/*******************************************************************************
 * Copyright (c) 2012 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat initial API and implementation
 *******************************************************************************/ 
package org.eclipse.linuxtools.profiling.snapshot.launch;

import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.linuxtools.profiling.launch.ProfileLaunchShortcut;

public class SnapshotLaunchShortcut extends ProfileLaunchShortcut {

	private static final String SNAPSHOT = "snapshot"; //$NON-NLS-1$

	@Override
	public void launch(IBinary bin, String mode) {
		ProfileLaunchShortcut provider = getProfilingProvider(SNAPSHOT);
		if (provider != null){
			provider.launch(bin, mode);
		}else{
			handleFail(Messages.SnapshotLaunchShortcut_0 + SNAPSHOT);
		}
	}

	@Override
	protected ILaunchConfigurationType getLaunchConfigType() {
		return null;
	}

	@Override
	protected void setDefaultProfileAttributes(
			ILaunchConfigurationWorkingCopy wc) throws CoreException {
	}

}
