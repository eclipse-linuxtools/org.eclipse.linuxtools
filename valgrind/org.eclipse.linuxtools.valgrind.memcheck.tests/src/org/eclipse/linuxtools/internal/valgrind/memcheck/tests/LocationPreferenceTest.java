package org.eclipse.linuxtools.internal.valgrind.memcheck.tests;

import java.io.IOException;

import junit.framework.TestCase;

import org.eclipse.linuxtools.internal.valgrind.core.ValgrindCommand;
import org.eclipse.linuxtools.internal.valgrind.core.ValgrindPlugin;
import org.eclipse.linuxtools.internal.valgrind.core.ValgrindPreferencePage;
import org.eclipse.linuxtools.internal.valgrind.launch.ValgrindLaunchPlugin;


public class LocationPreferenceTest extends TestCase {

	private ValgrindCommand command = new ValgrindCommand() {
		protected void readIntoBuffer(StringBuffer out, Process p) throws IOException {
			// Simulate not finding Valgrind in the user's PATH
			throw new IOException();
		}
	};
	
	/**
	 * Tests Bug #315890 - Valgrind location cannot be overridden unless Valgrind is present in PATH
	 * @throws Exception
	 */
	public void testManualLocationNoPATH() throws Exception {
		// Set a preference for a manual location
		ValgrindPlugin.getDefault().getPreferenceStore().setValue(ValgrindPreferencePage.VALGRIND_PATH, "/path/to/valgrind");
		
		ValgrindLaunchPlugin.getDefault().setValgrindCommand(command);
		
		ValgrindLaunchPlugin.getDefault().getValgrindLocation();
	}
}
