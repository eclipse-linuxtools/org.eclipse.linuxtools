/*******************************************************************************
 * Copyright (c) 2004,2008 Red Hat, Inc.
 * (C) Copyright 2010 IBM Corp. 2010
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Keith Seitz <keiths@redhat.com> - initial API and implementation
 *    Kent Sebastian <ksebasti@redhat.com> - 
 *    Thavidu Ranatunga (IBM) - derived from
 *       org.eclipse.linuxtools.oprofile.launch.configuration.OprofileSetupTab
 *******************************************************************************/
package org.eclipse.linuxtools.internal.perf.remote.launch;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.linuxtools.internal.perf.PerfCore;
import org.eclipse.linuxtools.internal.perf.PerfPlugin;
import org.eclipse.linuxtools.profiling.launch.ConfigUtils;

public class PerfOptionsTab extends org.eclipse.linuxtools.internal.perf.launch.PerfOptionsTab {

	private Exception ex;

	@Override
	public void initializeFrom(ILaunchConfiguration config) {
		ConfigUtils configUtils = new ConfigUtils(config);
		IProject project = null;

		try {
			project = ConfigUtils.getProject(configUtils.getProjectName());
		} catch (CoreException e1) {
			e1.printStackTrace();
		}
		if (!PerfCore.checkRemotePerfInPath(project)) {
			IStatus status = new Status(IStatus.ERROR, PerfPlugin.PLUGIN_ID, "Error: Perf was not found on PATH"); //$NON-NLS-1$
			ex = new CoreException(status);
		}
		super.initializeFrom(config);
	}

	@Override
	public boolean isValid (ILaunchConfiguration config) {
		if (ex != null) {
			setErrorMessage(ex.getLocalizedMessage());
			return false;
		}
		return super.isValid(config);
	}
}
