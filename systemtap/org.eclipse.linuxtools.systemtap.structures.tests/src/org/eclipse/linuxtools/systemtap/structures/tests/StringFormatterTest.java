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

package org.eclipse.linuxtools.systemtap.structures.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
		assertEquals("0x1000", formatter.format("8"));

		formatter.setFormat(IFormattingStyles.HEX);
		assertEquals("0x8", formatter.format("8"));

		formatter.setFormat(IFormattingStyles.OCTAL);
		assertEquals("0x10",formatter.format("8"));

		formatter.setFormat(IFormattingStyles.STRING);
		assertEquals("8", formatter.format("8"));

		formatter.setFormat(IFormattingStyles.UNFORMATED);
		assertEquals("8", formatter.format("8"));

		formatter.setFormat(IFormattingStyles.DATE);
		assertEquals("Dec 31, 1969 4:00:00 PM", formatter.format("8"));

		formatter.setFormat(IFormattingStyles.DOUBLE);
		assertEquals("8.0", formatter.format("8"));
	}

	private StringFormatter formatter;
}
