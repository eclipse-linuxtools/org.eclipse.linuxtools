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

package org.eclipse.linuxtools.systemtap.graphing.core.tests.aggregates;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.eclipse.linuxtools.systemtap.graphing.core.aggregates.CountAggregate;
import org.eclipse.linuxtools.systemtap.graphing.core.tests.MockDataSet;
import org.junit.Test;

public class CountAggregateTest {

    @Test
    public void testAggregate() {
        CountAggregate aa = new CountAggregate();
        Number num;

        num = aa.aggregate(null);
        assertNull(num);

        num = aa.aggregate(new Number[] {});
        assertNull(num);

        num = aa.aggregate(MockDataSet.buildIntegerArray(new int[] {0,0,0}));
        assertEquals(3, num.intValue());

        num = aa.aggregate(MockDataSet.buildIntegerArray(new int[] {-1,0}));
        assertEquals(2, num.intValue());

        num = aa.aggregate(MockDataSet.buildIntegerArray(new int[] {0,0,1,2,3}));
        assertEquals(5, num.intValue());


        num = aa.aggregate(MockDataSet.buildDoubleArray(new double[] {0,0,0}));
        assertEquals(3, num.doubleValue(), 0.0);

        num = aa.aggregate(MockDataSet.buildDoubleArray(new double[] {-1,1}));
        assertEquals(2, num.doubleValue(), 0.0);

        num = aa.aggregate(MockDataSet.buildDoubleArray(new double[] {0,0,1,4,5}));
        assertEquals(5, num.doubleValue(), 0.0);
    }

    @Test
    public void testGetID() {
        CountAggregate aa = new CountAggregate();
        assertEquals(CountAggregate.ID, aa.getID());
    }

}
