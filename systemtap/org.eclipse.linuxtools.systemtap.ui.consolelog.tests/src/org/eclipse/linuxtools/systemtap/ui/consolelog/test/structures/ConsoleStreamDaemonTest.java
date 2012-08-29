/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.ui.consolelog.test.structures;

import org.eclipse.linuxtools.systemtap.ui.structures.runnable.StreamGobbler;
import org.eclipse.linuxtools.systemtap.ui.consolelog.structures.ConsoleStreamDaemon;
import org.eclipse.linuxtools.systemtap.ui.consolelog.structures.ScriptConsole;

import junit.framework.TestCase;

public class ConsoleStreamDaemonTest extends TestCase {
	public ConsoleStreamDaemonTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		StreamGobbler gobbler = new StreamGobbler(System.in);
		gobbler.start();
		daemon = new ConsoleStreamDaemon(null);
	}
	
	public void testConsoleStreamDaemon() {
		assertNotNull(daemon);

		ConsoleStreamDaemon csd = new ConsoleStreamDaemon(null);
		assertNotNull(csd);
		
		csd = new ConsoleStreamDaemon(ScriptConsole.getInstance("test"));
		assertNotNull(csd);
	}
	
	public void testHandleDataEvent() {
		daemon.handleDataEvent("");
		assertNotNull(daemon);
	}

	public void testIsDisposed() {
		ConsoleStreamDaemon csd = new ConsoleStreamDaemon(null);
		assertFalse(csd.isDisposed());
		csd.dispose();
		assertTrue(csd.isDisposed());
	}
	
	public void testDispose() {
		daemon.dispose();
		assertNotNull(daemon);
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	private ConsoleStreamDaemon daemon;
}