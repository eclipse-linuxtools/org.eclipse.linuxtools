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

import org.eclipse.debug.core.ILaunch;
import org.eclipse.linuxtools.internal.oprofile.core.daemon.OprofileDaemonEvent;
import org.eclipse.linuxtools.internal.oprofile.core.daemon.OprofileDaemonOptions;
import org.eclipse.linuxtools.internal.oprofile.launch.configuration.LaunchOptions;
import org.eclipse.linuxtools.internal.oprofile.launch.launching.OprofileLaunchConfigurationDelegate;

/**
 * Helper delegate class
 * 
 * @author Red Hat Inc.
 *
 */
public final class TestingOprofileLaunchConfigurationDelegate extends OprofileLaunchConfigurationDelegate {
	public boolean eventsIsNull;
	public OprofileDaemonOptions _options;  
	protected void oprofileDumpSamples() { return; }
	protected void oprofileReset() { return; }
	protected void oprofileShutdown() { return; }
	protected void oprofileStartCollection() { return; }
	protected void oprofileSetupDaemon(OprofileDaemonOptions options, OprofileDaemonEvent[] events) { 
		_options = options; 
		eventsIsNull = events == null ? true : false; 
		return; 
	}
	@Override
	protected void postExec(LaunchOptions options, OprofileDaemonEvent[] daemonEvents, ILaunch launch, Process process) {
		super.postExec(options, daemonEvents, launch, process);
		
		try {
			process.waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
	}
}
