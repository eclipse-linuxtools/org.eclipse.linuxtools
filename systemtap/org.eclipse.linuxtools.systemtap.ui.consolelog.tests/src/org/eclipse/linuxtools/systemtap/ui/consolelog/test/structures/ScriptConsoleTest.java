package org.eclipse.linuxtools.systemtap.ui.consolelog.test.structures;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import org.eclipse.linuxtools.systemtap.ui.consolelog.structures.ScriptConsole;
import org.junit.Before;
import org.junit.Test;

public class ScriptConsoleTest {

	@Before
	public void setUp() {
		console = ScriptConsole.getInstance("test");
	}
	@Test
	public void testGetInstance() {
		assertNotNull(console);
		assertSame(console, ScriptConsole.getInstance("test"));
		ScriptConsole console2 = ScriptConsole.getInstance("a");
		assertNotNull(console2);
		assertNotSame(console, console2);
	}
	
	ScriptConsole console;
}
