package org.eclipse.linuxtools.systemtap.ui.structures.runnable;

import org.eclipse.linuxtools.systemtap.ui.structures.runnable.LoggedCommand;

import junit.framework.TestCase;

public class LoggedCommandTest extends TestCase {
	public LoggedCommandTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		cmd = new LoggedCommand(new String[] {"stap", "-v", "-p1", "-e", "probe nosuchfunc{}"}, null, null);
	}

	public void testLoggedCommand() {
		cmd.dispose();

		cmd = new LoggedCommand(new String[] {"stap", "-v", "-p1", "-e", "probe nosuchfunc{}"}, null, null);
		cmd.start();
		assertTrue(cmd.isRunning());
		assertFalse(cmd.isDisposed());
		cmd.stop();
		assertFalse(cmd.isRunning());
		assertFalse(cmd.isDisposed());
		cmd.dispose();

		cmd = new LoggedCommand(new String[] {"stap", "-v", "-p1", "-e", "probe nosuchfunc{}"}, null, null, 100);
		cmd.start();
		assertTrue(cmd.isRunning());
		assertFalse(cmd.isDisposed());
		cmd.stop();
		assertFalse(cmd.isRunning());
		assertFalse(cmd.isDisposed());
		cmd.dispose();
	}
	
	public void testGetOutput() {
		
	}
	
	public void testSaveLog() {
		
	}
	
	public void testStop() {
		cmd.start();
		assertTrue(cmd.isRunning());
		cmd.stop();
		assertFalse(cmd.isRunning());
	}
	
	public void testDispose() {
		assertFalse(cmd.isDisposed());
		cmd.dispose();
		assertTrue(cmd.isDisposed());
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		cmd.dispose();
	}
	
	LoggedCommand cmd;
}
