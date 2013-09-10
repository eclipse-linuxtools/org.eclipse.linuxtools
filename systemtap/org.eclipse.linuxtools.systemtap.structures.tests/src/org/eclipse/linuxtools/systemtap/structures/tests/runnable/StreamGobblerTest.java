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

package org.eclipse.linuxtools.systemtap.structures.tests.runnable;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;

import org.eclipse.linuxtools.systemtap.structures.runnable.StreamGobbler;
import org.junit.Before;
import org.junit.Test;

public class StreamGobblerTest{

	private static class TestStream extends InputStream{
		int i = 10;
		@Override
		public int read() {
			if (i < 0)
				return -1;

			return i--;
		}
	}

	@Before
	public void setUp() {
		sg = new StreamGobbler(new TestStream());
		sg.start();
	}

	@Test
	public void testStreamGobbler() {
		assertNotNull("StreamGobbler not null", sg);

		sg = new StreamGobbler(null);
		assertNotNull("StreamGobbler not null", sg);

		sg = new StreamGobbler(new TestStream());
		assertNotNull("StreamGobbler not null", sg);
	}

	@Test
	public void testIsRunning() {
		assertTrue("StreamGobbler running", sg.isRunning());
		sg.stop();
		assertFalse("StreamGobbler stopped", sg.isRunning());
	}
	
	@Test
	public void testStop() {
		assertTrue("StreamGobbler running", sg.isRunning());
		sg.stop();
		assertFalse("StreamGobbler stopped", sg.isRunning());
	}
	
	@Test
	public void testDispose() {
		sg.dispose();
		assertFalse(sg.isRunning());
	}
	
	StreamGobbler sg;
}
