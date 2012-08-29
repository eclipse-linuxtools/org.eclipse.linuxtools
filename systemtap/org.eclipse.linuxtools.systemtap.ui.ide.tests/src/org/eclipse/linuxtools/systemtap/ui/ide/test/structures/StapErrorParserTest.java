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

package org.eclipse.linuxtools.systemtap.ui.ide.test.structures;

import org.eclipse.linuxtools.systemtap.ui.ide.structures.StapErrorParser;

import junit.framework.TestCase;

public class StapErrorParserTest extends TestCase {

	public static void main(String[] args) {
	}

	public StapErrorParserTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testStapErrorParser() {
		String[][] output;

		StapErrorParser parser = new StapErrorParser();
		
		output = parser.parseOutput(null);
		assertNull(output);

		output = parser.parseOutput("");
		assertNotNull(output);
		assertEquals(0, output.length);

		output = parser.parseOutput("this shouldn't have anything");
		assertNotNull(output);
		assertEquals(0, output.length);

		output = parser.parseOutput("parse error: expected identifier or '*' \n" +
						"saw: operator '{' at /home/morser/test.stp:14:7 \n" +
						"1 parse error(s). \n" +
						"Pass 1: parse failed.  Try again with more '-v' (verbose) options.");
		assertNotNull(output);
		assertEquals(1, output.length);
		assertTrue("parse error:".equals(output[0][0]));
		assertTrue(output[0][3].startsWith("14"));
		
		output = parser.parseOutput("semantic error: probe_615 with type mismatch for identifier 'flags' at /home/morser/test.stp:22:6: string vs. long \n" +
						"semantic error: probe_615 with type mismatch for identifier 'mode' at /home/morser/test.stp:23:6: string vs. long \n" +
						"semantic error: probe_615 with type mismatch for identifier 'f' at /home/morser/test.stp:25:6: string vs. long \n" +
						"Pass 2: analysis failed.  Try again with more '-v' (verbose) options.");
		assertNotNull(output);
		assertEquals(3, output.length);
		assertTrue("semantic error:".equals(output[0][0]));
		assertTrue(output[0][3].startsWith("22:6"));
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}
}