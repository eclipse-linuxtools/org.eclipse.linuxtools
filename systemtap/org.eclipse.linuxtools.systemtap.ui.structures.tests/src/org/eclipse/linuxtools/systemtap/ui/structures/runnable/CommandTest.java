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

package org.eclipse.linuxtools.systemtap.ui.structures.runnable;

import org.eclipse.linuxtools.systemtap.ui.structures.runnable.Command;

import junit.framework.TestCase;

public class CommandTest extends TestCase {
	public CommandTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		tc = new Command(new String[] {"ls", "/home/"}, null, null);
	}

	public void testCommand() {
		assertNotNull("Command not null", tc);

		tc = new Command(null, null, null);
		assertNotNull("Command not null", tc);
		
		tc = new Command(new String[] {}, null, null);
		assertNotNull("Command not null", tc);

		tc = new Command(new String[] {""}, null, null);
		assertNotNull("Command not null", tc);

		tc = new Command(new String[] {"a"}, null, null);
		assertNotNull("Command not null", tc);

		tc = new Command(new String[] {"ls", "/"}, null, null);
		assertNotNull("Command not null", tc);
	}

	public void testIsFinished() {
		assertTrue("Not finished", tc.isRunning());
		tc.stop();
		assertFalse("Finished", tc.isRunning());
	}
	
	public void testStop() {
		assertTrue("Running", tc.isRunning());
		tc.stop();
		assertFalse("Not running", tc.isRunning());
	}

	public void testGetReturnValue() {
		assertEquals(-1, tc.getReturnValue());
	}
	
	public void testIsDisposed() {
		assertFalse(tc.isDisposed());
		tc.dispose();
		assertTrue(tc.isDisposed());
	}
	
	public void testDispose() {
		tc.dispose();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	Command tc;
}

