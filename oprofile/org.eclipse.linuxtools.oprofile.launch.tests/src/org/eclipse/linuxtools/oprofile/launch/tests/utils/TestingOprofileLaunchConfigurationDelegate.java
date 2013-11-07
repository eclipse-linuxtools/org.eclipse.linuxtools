/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kent Sebastian <ksebasti@redhat.com> - initial API and implementation
 *    Severin Gehwolf <sgehwolf@redhat.com> - moved to separate class
 *******************************************************************************/

package org.eclipse.linuxtools.oprofile.launch.tests.utils;

import java.net.URI;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.linuxtools.internal.oprofile.core.daemon.OprofileDaemonEvent;
import org.eclipse.linuxtools.internal.oprofile.core.daemon.OprofileDaemonOptions;
import org.eclipse.linuxtools.internal.oprofile.launch.configuration.LaunchOptions;
import org.eclipse.linuxtools.internal.oprofile.launch.configuration.OprofileCounter;
import org.eclipse.linuxtools.internal.oprofile.launch.launching.OprofileLaunchConfigurationDelegate;

/**
 * Helper delegate class
 *
 * @author Red Hat Inc.
 *
 */
public final class TestingOprofileLaunchConfigurationDelegate extends
		OprofileLaunchConfigurationDelegate {
	public boolean eventsIsNull;
	public OprofileDaemonOptions _options;

	@Override
	protected void oprofileDumpSamples() {
		return;
	}

	@Override
	protected void oprofileReset() {
		return;
	}

	@Override
	protected void oprofileShutdown() {
		return;
	}

	@Override
	protected boolean oprofileStatus() {
		return false;
	}

	@Override
	protected void oprofileStartCollection() {
		return;
	}

	@Override
	protected void oprofileSetupDaemon(OprofileDaemonOptions options,
			OprofileDaemonEvent[] events) {
		_options = options;
		eventsIsNull = events == null ? true : false;
		return;
	}

	@Override
	protected void postExec(LaunchOptions options,
			OprofileDaemonEvent[] daemonEvents, Process process) {
		super.postExec(options, daemonEvents, process);

		try {
			process.waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected OprofileCounter[] oprofileCounters(ILaunchConfiguration config) {
		return new OprofileCounter[0];

	}

	@Override
	protected URI oprofileWorkingDirURI(ILaunchConfiguration config){
		return oprofileProject().getLocationURI();
	}
}
