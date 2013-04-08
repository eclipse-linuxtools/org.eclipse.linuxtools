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

package org.eclipse.linuxtools.systemtap.structures;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.linuxtools.systemtap.structures.IFormattingStyles;
import org.eclipse.linuxtools.systemtap.structures.StringFormatter;
import org.junit.Before;
import org.junit.Test;

public class StringFormatterTest {

	@Before
	public void setUp(){
		formatter = new StringFormatter();
	}

	@Test
	public void testStringFormatter() {
		formatter = new StringFormatter();
		assertNotNull(formatter);
	}
	
	@Test
	public void testGetFormat() {
		formatter.setFormat(IFormattingStyles.UNFORMATED);
		assertEquals(IFormattingStyles.UNFORMATED, formatter.getFormat());

		formatter.setFormat(IFormattingStyles.BINARY);
		assertEquals(IFormattingStyles.BINARY, formatter.getFormat());

		formatter.setFormat(IFormattingStyles.DOUBLE);
		assertEquals(IFormattingStyles.DOUBLE, formatter.getFormat());

		formatter.setFormat(IFormattingStyles.HEX);
		assertEquals(IFormattingStyles.HEX, formatter.getFormat());

		formatter.setFormat(IFormattingStyles.OCTAL);
		assertEquals(IFormattingStyles.OCTAL, formatter.getFormat());

		formatter.setFormat(IFormattingStyles.STRING);
		assertEquals(IFormattingStyles.STRING, formatter.getFormat());

		formatter.setFormat(IFormattingStyles.DATE);
		assertEquals(IFormattingStyles.DATE, formatter.getFormat());
	}
	
	public void testSetFormat() {
		formatter.setFormat(IFormattingStyles.BINARY);
		assertNotNull(formatter);
	}
	
	public void testFormat() {
		formatter.setFormat(IFormattingStyles.BINARY);
		assertTrue("0x1000".equals(formatter.format("8")));
		
		formatter.setFormat(IFormattingStyles.HEX);
		assertTrue("0x8".equals(formatter.format("8")));
		
		formatter.setFormat(IFormattingStyles.OCTAL);
		assertTrue("0x10".equals(formatter.format("8")));
		
		formatter.setFormat(IFormattingStyles.STRING);
		assertTrue("8".equals(formatter.format("8")));
		
		formatter.setFormat(IFormattingStyles.UNFORMATED);
		assertTrue("8".equals(formatter.format("8")));
		
		formatter.setFormat(IFormattingStyles.DATE);
		assertTrue("Dec 31, 1969 4:00:00 PM".equals(formatter.format("8")));
		
		formatter.setFormat(IFormattingStyles.DOUBLE);
		assertTrue("8.0".equals(formatter.format("8")));
	}

	StringFormatter formatter;
}
