package org.eclipse.linuxtools.systemtap.ui.consolelog.test.structures;

import org.eclipse.linuxtools.systemtap.ui.consolelog.structures.ScriptConsole;


import junit.framework.TestCase;

public class ScriptConsoleTest extends TestCase {

	public ScriptConsoleTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
				
		console = ScriptConsole.getInstance("test");
	}
	
	public void testGetInstance() {
		assertNotNull(console);
		assertSame(console, ScriptConsole.getInstance("test"));
		ScriptConsole console2 = ScriptConsole.getInstance("a");
		assertNotNull(console2);
		assertNotSame(console, console2);
	}
	
	public void testRun() {
		
	}
	
	public void testIsRunning() {
		
	}
	
	public void testIsDisposed() {
		
	}
	
	public void testSaveStream() {
		
	}
	
	public void testGetCommand() {
		
	}
	
	public void testStop() {
		
	}

	public void testDispose() {
		
	}
	
	public void testSetName() {
		
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	ScriptConsole console;
}
