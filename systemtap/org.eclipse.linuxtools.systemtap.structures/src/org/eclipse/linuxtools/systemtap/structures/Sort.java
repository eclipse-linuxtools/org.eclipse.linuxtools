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

public final class Sort {

    /**
     * Performs quicksort on the supplied list.
     *
     * @param list The list to sort.
     * @param p Recursed value, initially top value.
     * @param r Recursed value, initially bottom value.
     */
    public static void quicksort(Object[] list, int p, int r) {
        if(null == list) {
            return;
        } else if (p < r) {
            int q = partition(list,p,r);
            if (q == r) {
                q--;
            }

            quicksort(list,p,q);
            quicksort(list,q+1,r);
        }
    }

    /**
     * Partitions the input list, used by Quiksort.
     *
     * @param list The list to partition.
     * @param p Recursed value, initially top value.
     * @param r Recursed value, initially bottom value.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static int partition (Object[] list, int p, int r) {
        Comparable pivot = (list[p] instanceof Comparable ? (Comparable)list[p] : list[p].toString());
        int lo = p;
        int hi = r;

        while (true) {
            while (getComparable(list[hi]).compareTo(pivot) >= 0 && lo < hi) {
                hi--;
            }

            while (getComparable(list[lo]).compareTo(pivot) < 0 && lo < hi) {
                lo++;
            }

            if (lo < hi) {
                Object T = list[lo];
                list[lo] = list[hi];
                list[hi] = T;
            } else {
                return hi;
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private static Comparable getComparable(Object o) {
        return (o instanceof Comparable
                ? (Comparable)o
                        : o.toString());
    }
}
