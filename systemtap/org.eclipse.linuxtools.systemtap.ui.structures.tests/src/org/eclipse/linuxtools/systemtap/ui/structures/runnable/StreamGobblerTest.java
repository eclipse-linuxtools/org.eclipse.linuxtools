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

import org.eclipse.linuxtools.systemtap.ui.structures.runnable.StreamGobbler;

import junit.framework.TestCase;

public class StreamGobblerTest extends TestCase {
	public StreamGobblerTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		sg = new StreamGobbler(System.in);
		sg.start();
	}

	public void testStreamGobbler() {
		assertNotNull("StreamGobbler not null", sg);

		sg = new StreamGobbler(null);
		assertNotNull("StreamGobbler not null", sg);

		sg = new StreamGobbler(System.in);
		assertNotNull("StreamGobbler not null", sg);
	}

	public void testIsRunning() {
		assertTrue("StreamGobbler running", sg.isRunning());
		sg.stop();
		assertFalse("StreamGobbler stopped", sg.isRunning());
	}
	
	public void testStop() {
		assertTrue("StreamGobbler running", sg.isRunning());
		sg.stop();
		assertFalse("StreamGobbler stopped", sg.isRunning());
	}
	
	public void testDispose() {
		sg.dispose();
		assertFalse(sg.isRunning());
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	StreamGobbler sg;
}
