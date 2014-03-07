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

package org.eclipse.linuxtools.systemtap.graphing.core.tests.structures;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.linuxtools.systemtap.graphing.core.structures.NumberType;
import org.junit.Test;

public class NumberTypeTest {
	@Test
	public void testGetNumber() {
		Number n = NumberType.getNumber(3, 3.2);
		assertTrue(n instanceof Integer);
		assertEquals(3, n.intValue());

		n = NumberType.getNumber(3d, 3.2);
		assertTrue(n instanceof Double);
		assertEquals(3.2, n.doubleValue(), 0.0);

		n = NumberType.getNumber(3f, 3.2);
		assertTrue(n instanceof Float);
		assertEquals(3.2, n.floatValue(), 0.0001);

		n = NumberType.getNumber(3L, 3.2);
		assertTrue(n instanceof Long);
		assertEquals(3, n.longValue());

		n = NumberType.getNumber((byte)3, 3.2);
		assertTrue(n instanceof Byte);
		assertEquals(3, n.byteValue());

		n = NumberType.getNumber((short)3, 3.2);
		assertTrue(n instanceof Short);
		assertEquals(3, n.shortValue());
	}
	@Test
	public void testObj2num() {
		Object[] obj = new Object[] {3, 2.3d, 4.2f};
		Number[] num = NumberType.obj2num(obj);
		
		assertEquals(0, NumberType.obj2num("a").intValue());
		assertEquals(3, num.length);
		assertTrue(num[0] instanceof Integer);
		assertTrue(num[1] instanceof Double);
		assertTrue(num[2] instanceof Float);
		assertEquals(3, num[0].intValue());
		assertEquals(2.3, num[1].doubleValue(), 0.00001);
		assertEquals(4.2, num[2].doubleValue(), 0.00001);
	}
	@Test
	public void testCleanObj2Num() {
		assertEquals(3, NumberType.cleanObj2Num("3").intValue());
		assertEquals(3.2, NumberType.cleanObj2Num("3.2").doubleValue(), 0.00001);
		assertEquals(3, NumberType.cleanObj2Num(new Integer(3)).intValue());
	}
	
}
