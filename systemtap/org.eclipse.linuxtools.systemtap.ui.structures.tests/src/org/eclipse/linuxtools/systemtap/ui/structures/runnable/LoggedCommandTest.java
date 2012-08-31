package org.eclipse.linuxtools.systemtap.ui.structures.runnable;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class LoggedCommandTest {

	@Before
	protected void setUp() {
		cmd = new LoggedCommand(new String[] {"stap", "-v", "-p1", "-e", "probe nosuchfunc{}"}, null, null);
	}

	@Test
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
	
	@Test
	public void testGetOutput() {
		
	}
	
	@Test
	public void testSaveLog() {
		
	}
	
	@Test
	public void testStop() {
		cmd.start();
		assertTrue(cmd.isRunning());
		cmd.stop();
		assertFalse(cmd.isRunning());
	}
	
	@Test
	public void testDispose() {
		assertFalse(cmd.isDisposed());
		cmd.dispose();
		assertTrue(cmd.isDisposed());
	}
	
	@After
	protected void tearDown() {
		cmd.dispose();
	}
	
	LoggedCommand cmd;
}
