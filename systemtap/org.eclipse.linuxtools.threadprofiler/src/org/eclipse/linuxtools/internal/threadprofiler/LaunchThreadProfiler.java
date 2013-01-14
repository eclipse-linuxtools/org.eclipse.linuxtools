/*******************************************************************************
 * Copyright (c) 2010-2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.threadprofiler;

import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.linuxtools.internal.callgraph.launch.SystemTapLaunchShortcut;

public class LaunchThreadProfiler extends SystemTapLaunchShortcut{

	@Override
	public void launch(IBinary bin, String mode) {
		try {
			name = "Thread Profiler";
			ILaunchConfigurationWorkingCopy wc = createConfiguration(bin, name);
			outputPath = "/home/chwang/threadprofiler.output";
			binaryPath = bin.getResource().getLocation().toString();
			binaryPath = binaryPath.replaceAll("\\(", "\\\\\\(").replaceAll("\\)", "\\\\\\)").replaceAll(" ", "\\ ");
			arguments = binaryPath;
			
			finishLaunch(name, mode, wc);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public String setScriptPath() {
		return ThreadProfilerPlugin.getDefault().getPluginLocation() + "ThreadProfile.stp";
	}
	
	@Override
	public String setViewID() {
		return "org.eclipse.linuxtools.threadprofiler.threadprofilerview";
		
	}
	
	@Override
	public String setParserID() {
		return "org.eclipse.linuxtools.threadprofiler.threadparser";
	}
	
}
