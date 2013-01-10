/*******************************************************************************
 * Copyright (c) 2012 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.sleepingthreads.launch;

import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.linuxtools.internal.callgraph.launch.SystemTapLaunchShortcut;

public class LaunchSleepingThreads extends SystemTapLaunchShortcut {
	
	@Override
	public void launch(IBinary bin, String mode) {
		try {
			name = "sleeping threads";
			ILaunchConfigurationWorkingCopy wc = createConfiguration(bin, name);
			outputPath = "/home/chwang/sleeping.output";
			binaryPath = bin.getResource().getLocation().toString();
			arguments = binaryPath;
			finishLaunch(name, mode, wc);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String setScriptPath() {
		return "/home/chwang/testgraph.stp";
	}

	@Override
	public String setParserID() {
		return "org.eclipse.linuxtools.sleepingthreadparser";
	}
	
	@Override
	public String setViewID() {
		return "org.eclipse.linuxtools.sleepingthreads.xmlview";
	}
}
