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

package org.eclipse.linuxtools.systemtap.structures;


public class IndexedObject implements Comparable<IndexedObject>{
    public int index;
    public Object val;

    public IndexedObject(int i, Object v) {
        index = i;
        val = v;
    }

    @Override
    public String toString() {
        return val.toString();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public int compareTo(IndexedObject o) {
        if(o != null) {
            Comparable thisVal = (val instanceof Comparable) ? (Comparable)val : val.toString();
            Comparable otherVal = (o.val instanceof Comparable) ? (Comparable)o.val : o.val.toString();
            return thisVal.compareTo(otherVal);
        }
        return 0;
    }
}
