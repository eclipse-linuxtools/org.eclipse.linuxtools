package org.eclipse.linuxtools.profiling.snapshot.launch;

import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.linuxtools.profiling.launch.ProfileLaunchShortcut;

public class SnapshotLaunchShortcut extends ProfileLaunchShortcut {

	@Override
	public void launch(IBinary bin, String mode) {
		ProfileLaunchShortcut provider = getProfilingProvider("snapshot");
		provider.launch(bin, mode);
	}

	@Override
	protected ILaunchConfigurationType getLaunchConfigType() {
		return null;
	}

	@Override
	protected void setDefaultProfileAttributes(
			ILaunchConfigurationWorkingCopy wc) throws CoreException {
	}

}
