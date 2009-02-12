package org.eclipse.linuxtools.valgrind.tests;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.valgrind.launch.IValgrindToolPage;
import org.eclipse.linuxtools.valgrind.launch.ValgrindLaunchPlugin;

public abstract class ValgrindLaunchMockPlugin extends ValgrindLaunchPlugin {
	@Override
	public IValgrindToolPage getToolPage(String id) throws CoreException {
		return substitutePage();
	}
	
	public abstract IValgrindToolPage substitutePage();

}
