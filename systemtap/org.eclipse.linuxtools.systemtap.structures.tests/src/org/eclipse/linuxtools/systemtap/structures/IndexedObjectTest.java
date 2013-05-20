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

package org.eclipse.linuxtools.systemtap.structures;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.linuxtools.systemtap.structures.IndexedObject;
import org.junit.Before;
import org.junit.Test;

public class IndexedObjectTest{

	@Before
	public void setUp() {
		one = new IndexedObject(1, "one");
		two = new IndexedObject(2, "two");
		three = new IndexedObject(3, "three");
	}
	
	@Test
	public void testToString() {
		assertEquals("one", one.toString());
		assertEquals("two", two.toString());
		assertEquals("three", three.toString());
	}
	
	@Test
	public void testCompareTo() {
		assertEquals(0, one.compareTo(one));
		assertTrue(-1 >= one.compareTo(two));
		assertTrue(1 <= three.compareTo(one));
		assertEquals(0, one.compareTo(null));
	}
	
	IndexedObject one, two, three;
}
