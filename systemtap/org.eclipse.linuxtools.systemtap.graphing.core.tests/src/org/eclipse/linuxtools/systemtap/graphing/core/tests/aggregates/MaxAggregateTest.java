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

package org.eclipse.linuxtools.systemtap.graphing.core.tests.aggregates;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.eclipse.linuxtools.systemtap.graphing.core.aggregates.MaxAggregate;
import org.eclipse.linuxtools.systemtap.graphing.core.tests.MockDataSet;
import org.junit.Test;

public class MaxAggregateTest  {

    @Test
    public void testAggregate() {
        MaxAggregate aa = new MaxAggregate();
        Number num;

        num = aa.aggregate(null);
        assertNull(num);

        num = aa.aggregate(new Number[] {});
        assertNull(num);

        num = aa.aggregate(MockDataSet.buildIntegerArray(new int[] {0,0,0}));
        assertEquals(0, num.intValue());

        num = aa.aggregate(MockDataSet.buildIntegerArray(new int[] {-1,0,1}));
        assertEquals(1, num.intValue());

        num = aa.aggregate(MockDataSet.buildIntegerArray(new int[] {0,0,1}));
        assertEquals(1, num.intValue());

        num = aa.aggregate(MockDataSet.buildDoubleArray(new double[] {0,0,0}));
        assertEquals(0.0, num.doubleValue(), 0.0);

        num = aa.aggregate(MockDataSet.buildDoubleArray(new double[] {-1,0,1}));
        assertEquals(1.0, num.doubleValue(), 0.0);

        num = aa.aggregate(MockDataSet.buildDoubleArray(new double[] {0,0,1,2}));
        assertEquals(2.0, num.doubleValue(), 0.0);
    }

    @Test
    public void testGetID() {
        MaxAggregate aa = new MaxAggregate();
        assertEquals(MaxAggregate.ID, aa.getID());
    }

}
