package org.eclipse.linuxtools.systemtap.ui.structures;

import java.io.File;

import org.eclipse.linuxtools.systemtap.ui.structures.LoggingStreamDaemon;
import org.eclipse.linuxtools.systemtap.ui.structures.runnable.StreamGobbler;


import junit.framework.TestCase;

public class LoggingStreamDaemonTest extends TestCase {
	public LoggingStreamDaemonTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		StreamGobbler gobbler = new StreamGobbler(System.in);
		gobbler.start();
		daemon = new LoggingStreamDaemon();
	}

	public void testHandleDataEvent() {
		daemon.handleDataEvent("test");
	}
	
	public void testGetOutput() {
		assertTrue("".equals(daemon.getOutput()));

		daemon.handleDataEvent("test");
		assertTrue("test".equals(daemon.getOutput()));
	}
	
	public void testSaveLog() {
		File f = new File("/tmp/loggingstreamdaemon.test");
		assertTrue(daemon.saveLog(f));
		f.delete();

		daemon.handleDataEvent("test");
		assertTrue(daemon.saveLog(f));
		//assertTrue("test".equals(daemon.getOutput()));
		f.delete();
		
		f = new File("/root/");
		assertFalse(daemon.saveLog(f));
		f.delete();
	}
	
	public void testDispose() {
		daemon.dispose();
		assertNull(daemon.getOutput());
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	LoggingStreamDaemon daemon;
}
