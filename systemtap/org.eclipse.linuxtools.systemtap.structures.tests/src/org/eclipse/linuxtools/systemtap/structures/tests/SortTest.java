/*******************************************************************************
 * Copyright (c) 2006, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/
package org.eclipse.linuxtools.systemtap.structures.tests;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.eclipse.linuxtools.systemtap.structures.Sort;
import org.junit.jupiter.api.Test;

public class SortTest {

    @Test
    public void testQuicksort() {
        String blank = "";
        String[] s0 = null;
        String[] s1 = new String[0];
        String[] s2 = new String[] {blank};
        String[] s3 = new String[] {"a"};
        String[] s4 = new String[] {"a", "b", "c", "d"};
        String[] s5 = new String[] {"d", "c", "b", "a"};
        String[] s6 = new String[] {"c", "d", "a", "b"};
        String[] s7 = new String[] {"a", "d", "a", "b"};

        Sort.quicksort(s0, 0, 0);
        assertNull(s0, "Sort null list");

        Sort.quicksort(s1, 0, 0);
        assertEquals(0, s1.length, "Sort empty list");

        Sort.quicksort(s2, 0, s2.length-1);
        assertEquals(1, s2.length, "Sort blank list");
        assertEquals(blank, s2[0], "Blank item same");

        Sort.quicksort(s3, 0, s3.length-1);
        assertEquals(1, s3.length, "Sort single item list");
        assertEquals("a", s3[0], "Single item same");

        Sort.quicksort(s4, 0, s4.length-1);
        assertArrayEquals(new String[] {"a", "b", "c", "d"}, s4, "Sort ordered list");

        Sort.quicksort(s5, 0, s5.length-1);
        assertArrayEquals(new String[] {"a", "b", "c", "d"}, s5, "Sort reversed list");

        Sort.quicksort(s6, 0, s6.length-1);
        assertArrayEquals(new String[] {"a", "b", "c", "d"}, s6, "Sort random list");

        Sort.quicksort(s7, 0, s7.length-1);
        assertArrayEquals(new String[] {"a", "a", "b", "d"}, s7, "Sort duplicate item list");
    }
}
