package org.eclipse.linuxtools.sleepingthreads.launch;

import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.linuxtools.internal.callgraph.launch.SystemTapLaunchShortcut;

public class LaunchSleepingThreads extends SystemTapLaunchShortcut {
	
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
	
	public String setViewID() {
		return "org.eclipse.linuxtools.sleepingthreads.xmlview";
	}
}
