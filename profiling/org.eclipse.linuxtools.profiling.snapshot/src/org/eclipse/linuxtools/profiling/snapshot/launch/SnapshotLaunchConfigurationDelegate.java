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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.linuxtools.profiling.launch.ProfileLaunchConfigurationDelegate;

public class SnapshotLaunchConfigurationDelegate extends
		ProfileLaunchConfigurationDelegate {

	@Override
	protected String getPluginID() {
		return "org.eclipse.linuxtools.profiling.snapshot";
	}

	@Override
	public void launch(ILaunchConfiguration arg0, String arg1, ILaunch arg2,
			IProgressMonitor arg3) {
		return;
	}

	@Override
	public String generateCommand(ILaunchConfiguration config) {
		return null;
	}

}
