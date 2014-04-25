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
import static org.junit.Assert.assertNull;

import org.eclipse.linuxtools.systemtap.structures.Sort;
import org.junit.Test;


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
        assertNull("Sort null list", s0);

        Sort.quicksort(s1, 0, 0);
        assertEquals("Sort empty list", 0, s1.length);

        Sort.quicksort(s2, 0, s2.length-1);
        assertEquals("Sort blank list", 1, s2.length);
        assertEquals("Blank item same", blank, s2[0]);

        Sort.quicksort(s3, 0, s3.length-1);
        assertEquals("Sort single item list", 1, s3.length);
        assertEquals("Single item same", "a", s3[0]);

        Sort.quicksort(s4, 0, s4.length-1);
        assertEquals("Sort ordered list", 4, s4.length);
        assertEquals("Single item same", "a", s4[0]);
        assertEquals("Single item same", "b", s4[1]);
        assertEquals("Single item same", "c", s4[2]);
        assertEquals("Single item same", "d", s4[3]);

        Sort.quicksort(s5, 0, s5.length-1);
        assertEquals("Sort reversed list", 4, s5.length);
        assertEquals("Single item same", "a", s5[0]);
        assertEquals("Single item same", "b", s5[1]);
        assertEquals("Single item same", "c", s5[2]);
        assertEquals("Single item same", "d", s5[3]);

        Sort.quicksort(s6, 0, s6.length-1);
        assertEquals("Sort random list", 4, s6.length);
        assertEquals("Single item same", "a", s6[0]);
        assertEquals("Single item same", "b", s6[1]);
        assertEquals("Single item same", "c", s6[2]);
        assertEquals("Single item same", "d", s6[3]);

        Sort.quicksort(s7, 0, s7.length-1);
        assertEquals("Sort duplicate item list", 4, s7.length);
        assertEquals("Single item same", "a", s7[0]);
        assertEquals("Single item same", "a", s7[1]);
        assertEquals("Single item same", "b", s7[2]);
        assertEquals("Single item same", "d", s7[3]);
    }
}
