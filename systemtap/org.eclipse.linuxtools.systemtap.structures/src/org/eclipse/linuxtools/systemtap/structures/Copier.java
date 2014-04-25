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

import java.util.ArrayList;
import java.util.List;

public final class Copier {

    /**
     * Returns a copy of the passed in ArrayList array object.
     *
     * @param list The ArrayList array object to copy.
     *
     * @return The copy of the ArrayList array.
     * @since 3.0
     */
    public static <T> List<T>[] copy(List<T>[] list) {
        @SuppressWarnings("unchecked")
        List<T>[] list2 = new ArrayList[list.length];

        for(int i=0; i<list2.length; i++)
            list2[i] = copy(list[i]);

        return list2;
    }

    /**
     * Returns a copy of the passed in ArrayList object.
     *
     * @param list The ArrayList object to copy.
     *
     * @return The copy of the ArrayList.
     * @since 3.0
     */
    public static<T> List<T> copy(List<T> list) {

        ArrayList<T> list2 = new ArrayList<>();
        list2.addAll(list);
        return list2;
    }
}
