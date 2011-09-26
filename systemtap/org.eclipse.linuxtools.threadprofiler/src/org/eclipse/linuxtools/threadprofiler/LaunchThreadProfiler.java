package org.eclipse.linuxtools.threadprofiler;

import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.linuxtools.callgraph.launch.SystemTapLaunchShortcut;

public class LaunchThreadProfiler extends SystemTapLaunchShortcut{

	public void launch(IBinary bin, String mode) {
		try {
			name = "Thread Profiler";
			config = createConfiguration(bin, name);
			outputPath = "/home/chwang/threadprofiler.output";
			binaryPath = bin.getResource().getLocation().toString();
			binaryPath = binaryPath.replaceAll("\\(", "\\\\\\(").replaceAll("\\)", "\\\\\\)").replaceAll(" ", "\\ ");
			arguments = binaryPath;
			
			finishLaunch(name, mode, config.getWorkingCopy());
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
