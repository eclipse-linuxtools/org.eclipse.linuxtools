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

package org.eclipse.linuxtools.systemtap.graphing.core.aggregates;

import org.eclipse.linuxtools.systemtap.graphing.core.structures.NumberType;

public class MaxAggregate implements IDataAggregate {

    /**
     * Ensure column isn't empty, then get the maximum of all the column's values.
     *
     * @param column The column to search.
     *
     * @return Maximum of all the column's values.
     */
    @Override
    public Number aggregate(Number[] column) {
        if(column == null || column.length == 0) {
            return null;
        }

        double num = Double.NEGATIVE_INFINITY;

        for(int i=0; i<column.length; i++) {
            if(num < column[i].doubleValue()) {
                num = column[i].doubleValue();
            }
        }

        return NumberType.getNumber(column[0], num);
    }

    @Override
    public String getID() {
        return ID;
    }

    public static final String ID = "org.eclipse.linuxtools.systemtap.graphing.core.aggregates.MaxAggregate"; //$NON-NLS-1$
}
