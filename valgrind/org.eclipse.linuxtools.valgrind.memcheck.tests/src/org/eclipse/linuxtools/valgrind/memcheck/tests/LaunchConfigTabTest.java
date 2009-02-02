package org.eclipse.linuxtools.valgrind.memcheck.tests;

import java.util.Arrays;
import java.util.HashSet;

import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.ILaunchConfigurationTabGroup;
import org.eclipse.linuxtools.valgrind.launch.ValgrindLaunchConfigurationTabGroup;

public class LaunchConfigTabTest extends AbstractMemcheckTest {
	
	protected ILaunchConfigurationWorkingCopy wc;

	@Override
	protected void setUp() throws Exception {
		proj = createProject("basicTest"); //$NON-NLS-1$
		ILaunchConfigurationType configType = getLaunchConfigType();
		wc = configType.newInstance(null, "Test");		
		ILaunchConfigurationTabGroup tabGroup = new ValgrindLaunchConfigurationTabGroup();
		tabGroup.createTabs(null, ILaunchManager.PROFILE_MODE);
		tabGroup.setDefaults(wc);
	}
	
	@Override
	protected void tearDown() throws Exception {
		deleteProject(proj);
	}
	
	public void testDefaults() throws Exception {
		IBinary bin = proj.getBinaryContainer().getBinaries()[0];
		ILaunchConfiguration config = createConfiguration(bin);
		ILaunch launch = config.launch(ILaunchManager.PROFILE_MODE, null, true);
		
	}
	
}
