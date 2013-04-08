package org.eclipse.linuxtools.systemtap.ui.tests;

import java.io.IOException;

import org.eclipse.linuxtools.tools.launch.core.factory.RuntimeProcessFactory;
import org.junit.BeforeClass;

/**
 * Generic utilities for systemtap tests.
 */
public class SystemtapTest {
	public static boolean stapInstalled;

	@BeforeClass
	public static void checkStapInstalled() {
		stapInstalled = SystemtapTest.stapInstalled();
	}

	/**
	 * Check that stap is installed
	 *
	 * @return true if stap is installed, false otherwise.
	 */
	public static boolean stapInstalled() {
		try {
			Process process = RuntimeProcessFactory.getFactory().exec(
					new String[] { "stap", "-V" }, null);
			return (process != null);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
}
