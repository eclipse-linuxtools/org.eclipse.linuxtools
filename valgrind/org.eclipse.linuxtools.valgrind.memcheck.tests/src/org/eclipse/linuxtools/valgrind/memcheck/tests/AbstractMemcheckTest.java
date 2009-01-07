package org.eclipse.linuxtools.valgrind.memcheck.tests;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.linuxtools.profiling.tests.AbstractTest;
import org.eclipse.linuxtools.valgrind.core.utils.LaunchConfigurationConstants;
import org.eclipse.linuxtools.valgrind.launch.ValgrindLaunchPlugin;
import org.eclipse.linuxtools.valgrind.launch.ValgrindOptionsTab;
import org.eclipse.linuxtools.valgrind.memcheck.MemcheckPlugin;

public class AbstractMemcheckTest extends AbstractTest {

	@Override
	protected void setProfileAttributes(ILaunchConfigurationWorkingCopy wc)
			throws CoreException {
		ValgrindOptionsTab.setDefaultGeneralAttributes(wc);
		ILaunchConfigurationTab tab = ValgrindLaunchPlugin.getDefault().getToolPage(MemcheckPlugin.TOOL_ID);
		tab.setDefaults(wc);
		wc.setAttribute(LaunchConfigurationConstants.ATTR_TOOL, MemcheckPlugin.TOOL_ID);
	}

	@Override
	protected ILaunchConfigurationType getLaunchConfigType() {
		return getLaunchManager().getLaunchConfigurationType(ValgrindLaunchPlugin.LAUNCH_ID);
	}
	
	protected ICProject createProject(String projname) throws CoreException, URISyntaxException, InvocationTargetException, InterruptedException, IOException {
		return createProject(MemcheckTestsPlugin.getDefault().getBundle(), projname);
	}

}
