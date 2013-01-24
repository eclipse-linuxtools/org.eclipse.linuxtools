package org.eclipse.linuxtools.systemtap.ui.structures.runnable;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class LoggedCommandTest {

	@Before
	public void setUp() {
		cmd = new LoggedCommand(new String[] {"stap", "-v", "-p1", "-e", "probe nosuchfunc{}"}, null);
	}

	@Test
	public void testLoggedCommand() {
		cmd.dispose();

		cmd = new LoggedCommand(new String[] {"stap", "-v", "-p1", "-e", "probe nosuchfunc{}"}, null);
		cmd.start();
		assertTrue(cmd.isRunning());
		assertFalse(cmd.isDisposed());
		cmd.stop();
		assertFalse(cmd.isRunning());
		assertFalse(cmd.isDisposed());
		cmd.dispose();

		cmd = new LoggedCommand(new String[] {"stap", "-v", "-p1", "-e", "probe nosuchfunc{}"}, null);
		cmd.start();
		assertTrue(cmd.isRunning());
		assertFalse(cmd.isDisposed());
		cmd.stop();
		assertFalse(cmd.isRunning());
		assertFalse(cmd.isDisposed());
		cmd.dispose();
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
	public void tearDown() {
		cmd.dispose();
	}

	LoggedCommand cmd;
}
