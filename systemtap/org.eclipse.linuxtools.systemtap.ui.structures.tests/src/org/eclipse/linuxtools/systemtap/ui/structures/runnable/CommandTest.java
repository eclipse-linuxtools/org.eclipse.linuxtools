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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;


public class CommandTest {

	@Before
	public void setUp() {
		tc = new Command(new String[] {"ls", "/home/"}, null);
	}

	@Test
	public void testCommand() {
		assertNotNull("Command not null", tc);

		tc = new Command(null, null);
		assertNotNull("Command not null", tc);

		tc = new Command(new String[] {}, null);
		assertNotNull("Command not null", tc);

		tc = new Command(new String[] {""}, null);
		assertNotNull("Command not null", tc);

		tc = new Command(new String[] {"a"}, null);
		assertNotNull("Command not null", tc);

		tc = new Command(new String[] {"ls", "/"}, null);
		assertNotNull("Command not null", tc);
	}

	@Test
	public void testIsFinished() {
		assertTrue("Not finished", tc.isRunning());
		tc.stop();
		assertFalse("Finished", tc.isRunning());
	}

	@Test
	public void testStop() {
		assertTrue("Running", tc.isRunning());
		tc.stop();
		assertFalse("Not running", tc.isRunning());
	}

	@Test
	public void testGetReturnValue() {
		assertEquals(Integer.MAX_VALUE, tc.getReturnValue());
	}

	@Test
	public void testIsDisposed() {
		assertFalse(tc.isDisposed());
		tc.dispose();
		assertTrue(tc.isDisposed());
	}

	@Test
	public void testDispose() {
		tc.dispose();
	}

	Command tc;
}

