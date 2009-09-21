package org.eclipse.linuxtools.systemtap.local.launch.tests;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.linuxtools.profiling.tests.AbstractTest;
import org.eclipse.linuxtools.systemtap.local.core.LaunchConfigurationConstants;
import org.eclipse.linuxtools.systemtap.local.core.PluginConstants;
import org.eclipse.linuxtools.systemtap.local.launch.SystemTapLaunchConfigurationDelegate;
import org.eclipse.linuxtools.systemtap.local.launch.SystemTapOptionsTab;
import org.osgi.framework.Bundle;

public abstract class AbstractStapTest extends AbstractTest {
	protected ICProject proj;
	
	@Override
	protected ILaunchConfigurationType getLaunchConfigType() {
		return getLaunchManager().getLaunchConfigurationType(PluginConstants.CONFIGURATION_TYPE_ID);
	}

	@Override
	protected void setProfileAttributes(ILaunchConfigurationWorkingCopy wc)
			throws CoreException {
			ILaunchConfigurationTab tab = new SystemTapOptionsTab();
			tab.setDefaults(wc);
	}
	

	protected ICProject createProjectAndBuild(String projname) throws Exception {
		return createProjectAndBuild(getBundle(), projname);
	}

	protected abstract Bundle getBundle();

	protected ILaunch doLaunch(ILaunchConfiguration config, String testName) throws Exception {
		ILaunch launch;
		IPath pathToFiles = getPathToFiles(testName);
		ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
		wc.setAttribute(LaunchConfigurationConstants.OUTPUT_PATH, pathToFiles.toOSString() +"testName.o");
		wc.doSave();

		SystemTapLaunchConfigurationDelegate del = new SystemTapLaunchConfigurationDelegate();
		launch = new Launch(config, ILaunchManager.PROFILE_MODE, null);
		launches.add(launch);

		DebugPlugin.getDefault().getLaunchManager().addLaunch(launch);
		del.launch(config, ILaunchManager.PROFILE_MODE, launch, null);
		System.out.println(del.getCommand());

		return launch;
	}
	
	protected IPath getPathToFiles(String testName) throws URISyntaxException,
	IOException {
		URL location = FileLocator.find(getBundle(), new Path(""), null); //$NON-NLS-1$
		File file = new File(FileLocator.toFileURL(location).toURI());
		IPath pathToFiles = new Path(file.getCanonicalPath()).append(testName);
		return pathToFiles;
	}
	
	private List<ILaunch> launches;

	@Override
	protected void setUp() throws Exception {
		launches = new ArrayList<ILaunch>();

		super.setUp();
	}
	
	@Override
	protected void tearDown() throws Exception {
		if (launches.size() > 0) {
			DebugPlugin.getDefault().getLaunchManager().removeLaunches(launches.toArray(new ILaunch[launches.size()]));
			launches.clear();
		}
		super.tearDown();
	}
}
