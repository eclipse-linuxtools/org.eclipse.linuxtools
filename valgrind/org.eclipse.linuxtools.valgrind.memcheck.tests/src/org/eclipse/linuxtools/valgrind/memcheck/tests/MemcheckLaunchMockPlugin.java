package org.eclipse.linuxtools.valgrind.memcheck.tests;

import org.eclipse.linuxtools.valgrind.launch.IValgrindToolPage;
import org.eclipse.linuxtools.valgrind.tests.ValgrindLaunchMockPlugin;

public class MemcheckLaunchMockPlugin extends ValgrindLaunchMockPlugin {

	@Override
	public IValgrindToolPage substitutePage() {
		return new MemcheckTestToolPage();
	}

}
