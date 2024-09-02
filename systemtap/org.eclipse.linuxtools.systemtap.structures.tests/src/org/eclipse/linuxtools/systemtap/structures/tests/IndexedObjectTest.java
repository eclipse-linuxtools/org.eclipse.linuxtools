/*******************************************************************************
 * Copyright (c) 2009, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.structures.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.linuxtools.systemtap.structures.IndexedObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class IndexedObjectTest{

    @BeforeEach
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

    private IndexedObject one, two, three;
}
