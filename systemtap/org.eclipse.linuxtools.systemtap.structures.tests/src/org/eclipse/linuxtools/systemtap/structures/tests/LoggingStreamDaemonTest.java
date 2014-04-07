/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.structures.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.eclipse.linuxtools.systemtap.structures.LoggingStreamDaemon;
import org.eclipse.linuxtools.systemtap.structures.runnable.StreamGobbler;
import org.junit.After;
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
		assertTrue(daemon.getOutput().isEmpty());

		daemon.handleDataEvent("test");
		assertEquals("test", daemon.getOutput());
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

	@After
	public void tearDown() {
		daemon.dispose();
		assertNull(daemon.getOutput());
	}

	private LoggingStreamDaemon daemon;
}
