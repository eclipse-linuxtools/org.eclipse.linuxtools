package org.eclipse.linuxtools.systemtap.structures;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.eclipse.linuxtools.systemtap.structures.LoggingStreamDaemon;
import org.eclipse.linuxtools.systemtap.structures.runnable.StreamGobbler;
import org.junit.Before;
import org.junit.Test;

public class LoggingStreamDaemonTest {

	@Before
	public void setUp(){
		StreamGobbler gobbler = new StreamGobbler(System.in);
		gobbler.start();
		daemon = new LoggingStreamDaemon();
	}

	@Test
	public void testHandleDataEvent() {
		daemon.handleDataEvent("test");
	}
	
	@Test
	public void testGetOutput() {
		assertTrue("".equals(daemon.getOutput()));

		daemon.handleDataEvent("test");
		assertTrue("test".equals(daemon.getOutput()));
	}
	
	@Test
	public void testSaveLog() {
		File f = new File("/tmp/loggingstreamdaemon.test");
		assertTrue(daemon.saveLog(f));
		f.delete();

		daemon.handleDataEvent("test");
		assertTrue(daemon.saveLog(f));
		f.delete();
		
		f = new File("/root/");
		assertFalse(daemon.saveLog(f));
		f.delete();
	}
	
	@Test
	public void testDispose() {
		daemon.dispose();
		assertNull(daemon.getOutput());
	}
	
	LoggingStreamDaemon daemon;
}
